package rql.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import rql.RQLException;
import rql.RQLResponse;
import rql.RequestExecutor;
import rql.Response;
import rql.Statement;
import rql.impl.model.AssignmentExpression;
import rql.impl.model.QueryJoin;
import rql.impl.model.QueryJoinOn;
import rql.impl.model.QuerySelect;
import rql.impl.model.QueryStatement;
import rql.impl.model.QueryUnion;
import rql.impl.model.ResourceAlias;
import rql.impl.model.ResourceModel;
import rql.impl.model.SendStatement;
import rql.impl.model.StatementModel;
import rx.Observable;
import rx.exceptions.Exceptions;
import rx.functions.Func1;

public class StatementImpl implements Statement {

	private List<StatementModel> statements;
	private RequestExecutor executor;
	private StatementContext rootContext;

	public StatementImpl(RequestExecutor executor, List<StatementModel> statements) {
		this.executor = executor;
		this.statements = statements;
		this.rootContext = new StatementContext();
	}

	@Override
	public void addHeader(String name, String value) {
		this.rootContext.addHeader(name, value);
	}

	@Override
	public void addParameter(String name, String value) {
		this.rootContext.addParameter(name, value);
	}

	@Override
	public RQLResponse execute() throws RQLException {
		Observable<Response> stmtObervable = createStatementsObservable();
		return stmtObervable.buffer(statements.size()).flatMap(resps -> {
			RQLResponseImpl result = new RQLResponseImpl();
			for (Response resp : resps) {
				result.addResponse(resp);
			}
			return Observable.just(result);
		}).toBlocking().first();
	}

	private Observable<Response> createStatementsObservable() throws RQLException {
		Observable<Response> stmtObervable = null;
		for (StatementModel stmt : statements) {
			StatementContext context = rootContext.extend();
			if (stmtObervable == null) {
				stmtObervable = (Observable<Response>) createStatementObservable(context, stmt);
			} else {
				stmtObervable = stmtObervable.mergeWith(stmtObervable);
			}
		}
		return stmtObervable;
	}

	private Observable<? extends Response> createStatementObservable(StatementContext context, StatementModel model)
			throws RQLException {

		if (model.getResourceAlias() != null) {
			for (ResourceAlias ra : model.getResourceAlias()) {
				context.addResourceAlias(ra.getAlias(), ra.getResource());
			}
		}

		if (model.getHeaders() != null) {
			for (AssignmentExpression assign : model.getHeaders()) {
				context.addHeader(assign.getName(), assign.getValue());
			}
		}

		if (model.getParameters() != null) {
			for (AssignmentExpression assign : model.getParameters()) {
				context.addParameter(assign.getName(), assign.getValue());
			}
		}

		QueryStatement stmt = model.getQueryStatement();
		if (stmt instanceof QuerySelect) {
			return createSelectStatementObservable(context, (QuerySelect) stmt);
		} else if (stmt instanceof QueryUnion) {
			return createUnionStatementObservable(context, (QueryUnion) stmt);
		} else if (stmt instanceof QueryJoin) {
			return createJoinStatementObservable(context, (QueryJoin) stmt);
		}
		if (model.getSendStatement() != null) {
			return createSendStatementObservable(context, model);
		}
		return null;
	}

	private Observable<? extends Response> createSendStatementObservable(StatementContext context,
			StatementModel model) {
		SendStatement send = model.getSendStatement();
		ResourceModel resource = send.getResource();
		if (send.getResourceAlias() != null) {
			resource = context.getResource(send.getResourceAlias());
		}
		executor.requestAsync(context, resource);
		return Observable.just(null);
	}

	private Observable<JoinResponse> createJoinStatementObservable(StatementContext context, QueryJoin join)
			throws RQLException {

		JoinResponse joinResponse = new JoinResponse();

		return createSelectStatementObservable(context, join.getPrimary(), true).flatMap(resp -> {
			joinResponse.setPrimaryResponse(join.getPrimary().getAlias(), resp);
			return Observable.just(joinResponse);
		}).flatMap(resp -> {
			Observable<Pair<String, List<Map<String, Object>>>> joinObservable = null;
			try {
				for (QueryJoinOn joinOn : join.getJoins()) {
					String operand = null;
					for (AssignmentExpression condition : joinOn.getConditions()) {
						if (operand == null)
							operand = condition.getOperand();
						else if (!operand.equals(condition.getOperand()))
							throw Exceptions.propagate(new RQLException("Join condition must use one operand"));
					}
					Observable<Pair<String, List<Map<String, Object>>>> joinResult = null;
					List<Map<String, Object>> primaryResult = resp.getEntityAsListMap();
					if ("=".equals(operand)) {
						// if operand is '=', create separated resource query
						Observable<Pair<Response, Integer>> subResponse = Observable.empty();
						int i = 0;
						for (Map<String, Object> row : primaryResult) {
							StatementContext subContext = context.extend();
							for (AssignmentExpression condition : joinOn.getConditions()) {
								String primaryField = condition.getValue().split("\\.")[1];
								String joinField = condition.getName().split("\\.")[1];
								subContext.addParameter(joinField, row.get(primaryField).toString());
							}
							subContext.setAutoPopulateParams(true);
							int index = i++;
							subResponse = subResponse
									.mergeWith(createResourceObservable(subContext, joinOn.getResource()).flatMap(r -> {
										return Observable.just(Pair.of(r, index));
									}));
						}
						joinResult = subResponse.buffer(primaryResult.size()).flatMap(resps -> {
							return Observable.just(Pair.of(joinOn.getAlias(), resps.stream()
									.sorted((e1, e2) -> Integer.compare(e1.getValue(), e2.getValue())).map(r -> {
										try {
											return r.getKey().getEntityAsMap();
										} catch (Exception e) {
											throw Exceptions.propagate(new RQLException(e));
										}
									}).collect(Collectors.toList())));
						});
					} else {
						StatementContext subContext = context.extend();
						for (Map<String, Object> row : primaryResult) {
							for (AssignmentExpression condition : joinOn.getConditions()) {
								String primaryField = condition.getValue().split("\\.")[1];
								String joinField = condition.getName().split("\\.")[1];
								subContext.addParameter(joinField, row.get(primaryField).toString());
							}
						}
						subContext.setAutoPopulateParams(true);
						joinResult = createResourceObservable(subContext, joinOn.getResource()).flatMap(response -> {
							try {
								return Observable.just(Pair.of(joinOn.getAlias(), response.getEntityAsListMap()));
							} catch (Exception e) {
								throw Exceptions.propagate(new RQLException(e));
							}
						});
					}
					if (joinObservable == null) {
						joinObservable = joinResult;
					} else {
						joinObservable = joinObservable.mergeWith(joinResult);
					}
				}
			} catch (Exception e) {
				throw Exceptions.propagate(new RQLException(e));
			}
			return joinObservable.buffer(join.getJoins().size());
		}).flatMap(joins -> {
			for (Pair<String, List<Map<String, Object>>> r : joins)
				joinResponse.addResponse(r.getLeft(), r.getRight());
			return Observable.just(joinResponse);
		});
	}

	private Observable<UnionResponse> createUnionStatementObservable(StatementContext context, QueryUnion union) {
		Observable<SelectResponse> unionObservable = null;
		for (QuerySelect select : union.getSelects()) {
			if (unionObservable == null) {
				unionObservable = createSelectStatementObservable(context, select).flatMap(response -> {
					response.setResultAlias(select.getAlias());
					return Observable.just(response);
				});
			} else {
				unionObservable = unionObservable.mergeWith(
						unionObservable = createSelectStatementObservable(context, select).flatMap(response -> {
							response.setResultAlias(select.getAlias());
							return Observable.just(response);
						}));
			}
		}
		return unionObservable.buffer(union.getSelects().size()).flatMap(unions -> {
			UnionResponse unionResponse = new UnionResponse();
			for (SelectResponse select : unions)
				unionResponse.addResponse(select.getResultAlias(), select);
			return Observable.just(unionResponse);
		});
	}

	private Observable<SelectResponse> createSelectStatementObservable(StatementContext context, QuerySelect select) {
		return createSelectStatementObservable(context, select, false);
	}

	private Observable<SelectResponse> createSelectStatementObservable(StatementContext context, QuerySelect select,
			boolean selectAll) {
		ResourceModel resource = select.getResource();
		if (select.getResourceAlias() != null) {
			resource = context.getResource(select.getResourceAlias());
		}
		return createResourceObservable(context, resource).flatMap(new Func1<Response, Observable<SelectResponse>>() {
			@Override
			public Observable<SelectResponse> call(Response t) {
				SelectResponse selectResponse = new SelectResponse(t);
				selectResponse.setFields(select.getFields());
				if (selectAll)
					selectResponse.setSelectAll(selectAll);
				return Observable.just(selectResponse);
			}
		});
	}

	private Observable<Response> createResourceObservable(StatementContext context, ResourceModel resource) {
		return executor.request(context, resource);
	}

}

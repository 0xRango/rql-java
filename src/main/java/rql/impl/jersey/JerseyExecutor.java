package rql.impl.jersey;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.rx.RxClient;
import org.glassfish.jersey.client.rx.RxInvocationBuilder;
import org.glassfish.jersey.client.rx.RxWebTarget;
import org.glassfish.jersey.client.rx.rxjava.RxObservable;
import org.glassfish.jersey.client.rx.rxjava.RxObservableInvoker;

import rql.RequestExecutor;
import rql.Resource;
import rql.Response;
import rql.impl.StatementContext;
import rql.impl.model.AssignmentExpression;
import rx.Observable;
import rx.functions.Func1;

public class JerseyExecutor implements RequestExecutor {
	private RxClient<RxObservableInvoker> rxClient;

	public JerseyExecutor() {
		rxClient = RxObservable.newClient();
		rxClient.property(ClientProperties.CONNECT_TIMEOUT, 30000);
		rxClient.property(ClientProperties.READ_TIMEOUT, 30000);
	}

	@Override
	public Observable<Response> request(StatementContext context, Resource resource) {
		String uri = context.qualifyURL(resource.getUrl());
		RxWebTarget<RxObservableInvoker> webTarget = rxClient.target(uri);
		String method = resource.getMethod().toUpperCase();
		boolean entityPresence = "POST".equals(method) || "PUT".equals(method);

		if (!entityPresence && resource.getData() != null) {
			for (AssignmentExpression assign : resource.getData()) {
				webTarget = webTarget.queryParam(assign.getName(),
						assign.getValue() != null ? assign.getValue() : context.getParameter(assign.getVariable()));
			}
		}
		if (!entityPresence) {
			if (context.isAutoPopulateParams()) {
				for (String key : context.getParameters().keySet()) {
					for (Object val : context.getParameters().get(key)) {
						webTarget = webTarget.queryParam(key, val);
					}
				}
			}
		}

		RxInvocationBuilder<RxObservableInvoker> rx = webTarget.request(MediaType.APPLICATION_JSON);

		context.getHeaders().forEach((key, value) -> {
			value.forEach(val -> rx.header(key, val));
		});

		if (resource.getHeader() != null) {
			for (AssignmentExpression assign : resource.getHeader()) {
				if (assign.getValue() != null) {
					rx.header(assign.getName(), assign.getValue());
				} else {
					rx.header(assign.getName(), context.getParameter(assign.getVariable()));
				}
			}
		}

		Observable<javax.ws.rs.core.Response> requestObservalbe = null;

		// rx.property("index", index);

		if (entityPresence) {
			if (resource.getBody() != null) {
				requestObservalbe = rx.rx().method(resource.getMethod(),
						Entity.entity(context.getParameter(resource.getBody()), MediaType.APPLICATION_JSON));
			} else if (resource.getData() != null || context.isAutoPopulateParams()) {
				MultivaluedMap<String, Object> form = new MultivaluedHashMap<>();
				for (AssignmentExpression assign : resource.getData()) {
					form.add(assign.getName(),
							assign.getValue() != null ? assign.getValue() : context.getParameter(assign.getVariable()));
				}
				if (context.isAutoPopulateParams()) {
					context.getParameters().forEach((key, value) -> form.addAll(key, value));
				}
				requestObservalbe = rx.rx().method(resource.getMethod(),
						Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
			} else {
				requestObservalbe = rx.rx().method(resource.getMethod());
			}
		} else {
			requestObservalbe = rx.rx().method(resource.getMethod());
		}

		return requestObservalbe.flatMap(new Func1<javax.ws.rs.core.Response, Observable<Response>>() {
			@Override
			public Observable<Response> call(javax.ws.rs.core.Response t) {

				return Observable.just(new JerseyResponse(method, uri, t));
			}
		});
	}

	@Override
	public void requestAsync(StatementContext context, Resource resource) {
		RxWebTarget<RxObservableInvoker> webTarget = rxClient.target(context.qualifyURL(resource.getUrl()));
		String method = resource.getMethod().toUpperCase();
		boolean entityPresence = "POST".equals(method) || "PUT".equals(method);

		if (!entityPresence && resource.getData() != null) {
			for (AssignmentExpression assign : resource.getData()) {
				webTarget.queryParam(assign.getName(),
						assign.getValue() != null ? assign.getValue() : context.getParameter(assign.getVariable()));
			}
		}
		RxInvocationBuilder<RxObservableInvoker> rx = webTarget.request(MediaType.APPLICATION_JSON);
		if (resource.getHeader() != null) {
			for (AssignmentExpression assign : resource.getHeader()) {
				if (assign.getValue() != null) {
					rx.header(assign.getName(), assign.getValue());
				} else {
					rx.header(assign.getName(), context.getParameter(assign.getVariable()));
				}
			}
		}

		if (entityPresence) {
			if (resource.getBody() != null) {
				rx.async().method(resource.getMethod(),
						Entity.entity(context.getParameter(resource.getBody()), MediaType.APPLICATION_JSON));
			} else if (resource.getData() != null) {
				MultivaluedMap<String, String> form = new MultivaluedHashMap<>();
				for (AssignmentExpression assign : resource.getData()) {
					form.add(assign.getName(),
							assign.getValue() != null ? assign.getValue() : context.getParameter(assign.getVariable()));
				}
				rx.async().method(resource.getMethod(),
						Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
			} else {
				rx.async().method(resource.getMethod());
			}
		} else {
			rx.async().method(resource.getMethod());
		}
	}

}

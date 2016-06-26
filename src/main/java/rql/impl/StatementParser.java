package rql.impl;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ErrorNode;

import rql.antlr.RQLBaseListener;
import rql.antlr.RQLParser;
import rql.antlr.RQLParser.Alias_nameContext;
import rql.antlr.RQLParser.ConditionContext;
import rql.antlr.RQLParser.Field_nameContext;
import rql.antlr.RQLParser.Join_onContext;
import rql.antlr.RQLParser.LeftContext;
import rql.antlr.RQLParser.NameContext;
import rql.antlr.RQLParser.OperandContext;
import rql.antlr.RQLParser.Resource_nameContext;
import rql.antlr.RQLParser.RightContext;
import rql.antlr.RQLParser.VariableContext;
import rql.impl.model.AssignmentExpression;
import rql.impl.model.QueryField;
import rql.impl.model.QueryJoin;
import rql.impl.model.QueryJoinOn;
import rql.impl.model.QuerySelect;
import rql.impl.model.QueryUnion;
import rql.impl.model.ResourceAlias;
import rql.impl.model.ResourceModel;
import rql.impl.model.SendStatement;
import rql.impl.model.StatementModel;

class StatementParser extends RQLBaseListener {

	private List<StatementModel> statements = new ArrayList<StatementModel>();

	private StatementModel statement;

	private ResourceAlias resourceAlias;

	private ResourceModel resource;

	private QuerySelect querySelect;

	private SendStatement sendStatement;

	private QueryUnion queryUnion;

	private QueryJoin queryJoin;

	private QueryJoinOn joinOn;

	private QueryField queryField;

	@Override
	public void enterStatement(RQLParser.StatementContext ctx) {
		statement = new StatementModel();
	}

	@Override
	public void exitStatement(RQLParser.StatementContext ctx) {
		getStatements().add(statement);
		statement = null;
	}

	@Override
	public void enterResource_alias(RQLParser.Resource_aliasContext ctx) {
		resourceAlias = new ResourceAlias();
		resourceAlias.setAlias(ctx.ID().getText());
	}

	public void exitResource_alias(RQLParser.Resource_aliasContext ctx) {
		if (statement.getResourceAlias() == null)
			statement.setResourceAlias(new ArrayList<ResourceAlias>());
		statement.getResourceAlias().add(resourceAlias);
		resourceAlias = null;
	}

	@Override
	public void enterHeader(RQLParser.HeaderContext ctx) {
		if (statement.getHeaders() == null)
			statement.setHeaders(new ArrayList<AssignmentExpression>());
		AssignmentExpression expr = new AssignmentExpression();
		expr.setName(ctx.ID().getText());
		expr.setValue(getValue(ctx.VALUE().getText()));
		statement.getHeaders().add(expr);
	}

	private String getValue(String text) {
		return text.substring(1, text.length() - 1);
	}

	@Override
	public void enterParameter(RQLParser.ParameterContext ctx) {
		if (statement.getParameters() == null)
			statement.setParameters(new ArrayList<AssignmentExpression>());
		AssignmentExpression expr = new AssignmentExpression();
		expr.setName(ctx.ID().getText());
		expr.setValue(getValue(ctx.VALUE().getText()));
		statement.getParameters().add(expr);
	}

	@Override
	public void enterSelect(RQLParser.SelectContext ctx) {
		querySelect = new QuerySelect();

	}

	@Override
	public void exitSelect(RQLParser.SelectContext ctx) {
		if (queryUnion != null) {
			queryUnion.getSelects().add(querySelect);
		} else if (queryJoin != null) {
			queryJoin.setPrimary(querySelect);
		} else {
			statement.setQueryStatement(querySelect);
		}
		querySelect = null;
	}

	@Override
	public void enterField_name(Field_nameContext ctx) {
		queryField = new QueryField();
	}

	@Override
	public void exitField_name(Field_nameContext ctx) {
		String resource = null;
		String name = null;
		if (ctx.ID() != null) {
			name = ctx.ID().getText();
		} else {
			String fullname = ctx.FIELD_FULL() != null ? ctx.FIELD_FULL().getText() : ctx.FIELD_WILD().getText();
			String[] field = fullname.split("\\.");
			if (field.length > 1) {
				name = field[1];
				resource = field[0];
			} else {
				name = field[0];
			}
		}
		queryField.setResource(resource);
		queryField.setField(name);
		queryField.setAll(name.equals("*"));
		querySelect.getFields().add(queryField);
		queryField = null;
	}

	@Override
	public void enterUnion(RQLParser.UnionContext ctx) {
		queryUnion = new QueryUnion();
	}

	@Override
	public void exitUnion(RQLParser.UnionContext ctx) {
		statement.setQueryStatement(queryUnion);
		queryUnion = null;
	}

	@Override
	public void enterAlias_name(Alias_nameContext ctx) {
		String name = ctx.name().getText();
		if (queryField != null) {
			queryField.setAlias(name);
		} else if (querySelect != null) {
			querySelect.setAlias(name);
		} else if (joinOn != null) {
			joinOn.setAlias(name);
		} else if (queryJoin != null) {
			queryJoin.getPrimary().setAlias(name);
		}
	}

	@Override
	public void enterJoin(RQLParser.JoinContext ctx) {
		queryJoin = new QueryJoin();
	}

	@Override
	public void exitJoin(RQLParser.JoinContext ctx) {
		statement.setQueryStatement(queryJoin);
		queryJoin = null;
	}

	@Override
	public void enterJoin_on(Join_onContext ctx) {
		joinOn = new QueryJoinOn();
	}

	@Override
	public void exitJoin_on(Join_onContext ctx) {
		queryJoin.getJoins().add(joinOn);
		joinOn = null;
	}

	@Override
	public void enterCondition(ConditionContext ctx) {
		int i = 0;
		for (LeftContext left : ctx.left()) {
			RightContext right = ctx.right(i);
			OperandContext operand = ctx.operand(i);
			AssignmentExpression expr = new AssignmentExpression();
			expr.setName(left.getText());
			expr.setValue(right.getText());
			expr.setOperand(operand.getText());
			joinOn.getConditions().add(expr);
			i++;
		}
	}

	@Override
	public void enterSend(RQLParser.SendContext ctx) {
		sendStatement = new SendStatement();
	}

	@Override
	public void exitSend(RQLParser.SendContext ctx) {
		statement.setSendStatement(sendStatement);
		sendStatement = null;
	}

	@Override
	public void enterResource_name(Resource_nameContext ctx) {
		if (querySelect != null)
			querySelect.setResourceAlias(ctx.getText());
		else if (sendStatement != null)
			sendStatement.setResourceAlias(ctx.getText());
	}

	@Override
	public void enterResource(RQLParser.ResourceContext ctx) {
		resource = new ResourceModel();
		resource.setMethod(ctx.method().getText());
		resource.setUrl(ctx.uri().getText());
		if (ctx.body() != null)
			resource.setBody(ctx.body().ID().getText());
		if (ctx.header_expr() != null) {
			int i = 0;
			for (NameContext name : ctx.header_expr().name()) {
				VariableContext variable = ctx.header_expr().variable(i++);
				if (resource.getHeader() == null) {
					resource.setHeader(new ArrayList<AssignmentExpression>());
				}
				resource.getHeader().add(createExpression(name, variable));
			}
		}
		if (ctx.data_expr() != null) {
			int i = 0;
			for (NameContext name : ctx.data_expr().name()) {
				VariableContext variable = ctx.data_expr().variable(i++);
				if (resource.getData() == null) {
					resource.setData(new ArrayList<AssignmentExpression>());
				}
				resource.getData().add(createExpression(name, variable));
			}
		}
	}

	@Override
	public void exitResource(RQLParser.ResourceContext ctx) {
		if (resourceAlias != null) {
			resourceAlias.setResource(resource);
		} else if (querySelect != null) {
			querySelect.setResource(resource);
		} else if (sendStatement != null) {
			sendStatement.setResource(resource);
		} else if (joinOn != null) {
			joinOn.setResource(resource);
		}
		resource = null;
	}

	private AssignmentExpression createExpression(NameContext name, VariableContext variable) {
		AssignmentExpression expr = new AssignmentExpression();
		expr.setName(name.getText());
		if (variable.ID() != null) {
			expr.setVariable(variable.ID().getText());
		} else if (variable.VALUE() != null) {
			expr.setValue(variable.VALUE().getText());
		}
		return expr;
	}

	@Override
	public void visitErrorNode(ErrorNode node) {
	}

	public List<StatementModel> getStatements() {
		return statements;
	}

}

package rql.impl.model;

import java.util.List;

public class StatementModel {
	private List<ResourceAlias> resourceAlias;
	private List<AssignmentExpression> headers;
	private List<AssignmentExpression> parameters;
	private QueryStatement queryStatement;
	private SendStatement sendStatement;

	public List<ResourceAlias> getResourceAlias() {
		return resourceAlias;
	}

	public void setResourceAlias(List<ResourceAlias> resourceAlias) {
		this.resourceAlias = resourceAlias;
	}

	public List<AssignmentExpression> getHeaders() {
		return headers;
	}

	public void setHeaders(List<AssignmentExpression> headers) {
		this.headers = headers;
	}

	public List<AssignmentExpression> getParameters() {
		return parameters;
	}

	public void setParameters(List<AssignmentExpression> parameters) {
		this.parameters = parameters;
	}

	public QueryStatement getQueryStatement() {
		return queryStatement;
	}

	public void setQueryStatement(QueryStatement queryStatement) {
		this.queryStatement = queryStatement;
	}

	public SendStatement getSendStatement() {
		return sendStatement;
	}

	public void setSendStatement(SendStatement sendStatement) {
		this.sendStatement = sendStatement;
	}
}
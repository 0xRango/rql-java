package rql.impl.model;

import java.util.ArrayList;
import java.util.List;

public class QueryJoinOn extends QueryStatement {

	private ResourceModel resource;
	private String alias;
	private List<AssignmentExpression> conditions = new ArrayList<AssignmentExpression>();

	public ResourceModel getResource() {
		return resource;
	}

	public void setResource(ResourceModel resource) {
		this.resource = resource;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public List<AssignmentExpression> getConditions() {
		return conditions;
	}

	public void setConditions(List<AssignmentExpression> conditions) {
		this.conditions = conditions;
	}

}
package rql;

import java.util.List;

import rql.impl.model.AssignmentExpression;

public interface Resource {

	public String getMethod();

	public String getUrl();

	public String getBody();

	public List<AssignmentExpression> getHeader();

	public List<AssignmentExpression> getData();
}

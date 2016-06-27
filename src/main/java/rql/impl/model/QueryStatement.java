package rql.impl.model;

public abstract class QueryStatement {
	
	private String alias;

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
	
}
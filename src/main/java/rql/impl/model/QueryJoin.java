package rql.impl.model;

import java.util.ArrayList;
import java.util.List;

public class QueryJoin extends QueryStatement {

	private QuerySelect primary;
	private String alias;
	private List<QueryJoinOn> joins = new ArrayList<QueryJoinOn>();

	public QuerySelect getPrimary() {
		return primary;
	}

	public void setPrimary(QuerySelect primary) {
		this.primary = primary;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public List<QueryJoinOn> getJoins() {
		return joins;
	}

	public void setJoins(List<QueryJoinOn> joins) {
		this.joins = joins;
	}

}
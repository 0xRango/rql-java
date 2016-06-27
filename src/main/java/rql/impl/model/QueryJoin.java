package rql.impl.model;

import java.util.ArrayList;
import java.util.List;

public class QueryJoin extends QueryStatement {

	private QueryStatement primary;
	private List<QueryJoinOn> joins = new ArrayList<QueryJoinOn>();

	public QueryStatement getPrimary() {
		return primary;
	}

	public void setPrimary(QueryStatement primary) {
		this.primary = primary;
	}

	public List<QueryJoinOn> getJoins() {
		return joins;
	}

	public void setJoins(List<QueryJoinOn> joins) {
		this.joins = joins;
	}

}
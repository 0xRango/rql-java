package rql.impl.model;

import java.util.ArrayList;
import java.util.List;

public class QueryUnion extends QueryStatement {

	private List<QueryStatement> selects = new ArrayList<QueryStatement>();

	public List<QueryStatement> getSelects() {
		return selects;
	}

}
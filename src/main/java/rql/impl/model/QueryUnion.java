package rql.impl.model;

import java.util.ArrayList;
import java.util.List;

public class QueryUnion extends QueryStatement {

	private List<QuerySelect> selects = new ArrayList<QuerySelect>();

	public List<QuerySelect> getSelects() {
		return selects;
	}

}
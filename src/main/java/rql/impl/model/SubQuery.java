package rql.impl.model;

import java.util.ArrayList;
import java.util.List;

public class SubQuery extends QueryStatement{

	private List<QueryField> fields = new ArrayList<QueryField>();
	
	private QuerySelect select;
	
	private String alias;

	public List<QueryField> getFields() {
		return fields;
	}

	public void setFields(List<QueryField> fields) {
		this.fields = fields;
	}

	public QuerySelect getSelect() {
		return select;
	}

	public void setSelect(QuerySelect select) {
		this.select = select;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	
}
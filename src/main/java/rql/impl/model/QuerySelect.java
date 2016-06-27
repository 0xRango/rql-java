package rql.impl.model;

import java.util.ArrayList;
import java.util.List;

public class QuerySelect extends QueryStatement {

	private List<QueryField> fields = new ArrayList<QueryField>();

	private ResourceModel resource;

	private String resourceAlias;
	
	public ResourceModel getResource() {
		return resource;
	}

	public void setResource(ResourceModel resource) {
		this.resource = resource;
	}

	public String getResourceAlias() {
		return resourceAlias;
	}

	public void setResourceAlias(String resourceAlias) {
		this.resourceAlias = resourceAlias;
	}

	public List<QueryField> getFields() {
		return fields;
	}


}
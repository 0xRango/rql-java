package rql.impl.model;

public class SendStatement {
	
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
	
}
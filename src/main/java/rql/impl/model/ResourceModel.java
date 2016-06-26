package rql.impl.model;

import java.util.List;

import rql.Resource;

public class ResourceModel implements Resource {

	private String method;
	private String url;
	private String body;
	private List<AssignmentExpression> header;
	private List<AssignmentExpression> data;

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public List<AssignmentExpression> getHeader() {
		return header;
	}

	public void setHeader(List<AssignmentExpression> header) {
		this.header = header;
	}

	public List<AssignmentExpression> getData() {
		return data;
	}

	public void setData(List<AssignmentExpression> data) {
		this.data = data;
	}

}

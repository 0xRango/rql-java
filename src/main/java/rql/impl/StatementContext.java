package rql.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import rql.impl.model.ResourceModel;

public class StatementContext {

	private MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

	private MultivaluedMap<String, Object> parameters = new MultivaluedHashMap<>();

	private Map<String, ResourceModel> resourceAlias = new HashMap<>();

	private StatementContext parent;
	
	private boolean autoPopulateParams;

	StatementContext(StatementContext parent) {
		this.parent = parent;
	}

	public StatementContext() {
	}

	public StatementContext extend() {
		return new StatementContext(this);
	}

	public MultivaluedMap<String, Object> getHeaders() {
		return headers;
	}

	public MultivaluedMap<String, Object> getParameters() {
		return parameters;
	}

	public void addHeader(String name, String value) {
		headers.add(name, value);
	}

	public void addParameter(String name, String value) {
		parameters.add(name, value);
	}

	public String qualifyURL(String url) {
		Pattern p = Pattern.compile("(\\{\\.+\\})");
		Matcher m = p.matcher(url);
		while (m.find()) {
			String val1 = m.group().replace("{", "").replace("}", "");
			Object param = getParameter(val1);
			if (param != null)
				url = url.replace(m.group(), param.toString());
		}
		return url;
	}

	public Object getHeader(String key) {
		Object value = headers.getFirst(key);
		if (value == null && parent != null)
			value = this.parent.getHeader(key);
		return value == null ? null : value.toString();
	}

	public String getParameter(String key) {
		Object value = parameters.getFirst(key);
		if (value == null && parent != null)
			value = this.parent.getParameter(key);
		return value == null ? null : value.toString();
	}

	public void addResourceAlias(String alias, ResourceModel resource) {
		resourceAlias.put(alias, resource);
	}

	public ResourceModel getResource(String alias) {
		return resourceAlias.get(alias);
	}

	public boolean isAutoPopulateParams() {
		return autoPopulateParams;
	}

	public void setAutoPopulateParams(boolean autoPopulateParams) {
		this.autoPopulateParams = autoPopulateParams;
	}

}

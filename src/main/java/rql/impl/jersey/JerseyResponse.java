package rql.impl.jersey;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MultivaluedMap;

import rql.RQLException;
import rql.Response;

public class JerseyResponse implements Response {

	private javax.ws.rs.core.Response resp;
	private Map<String, Object> entityAsMap;
	private List<Map<String, Object>> entityAsListMap;
	private List entityAsList;
	private Object entityAsObject;
	private String method;
	private String uri;

	public JerseyResponse(String method, String uri, javax.ws.rs.core.Response resp) {
		this.method = method;
		this.uri = uri;
		this.resp = resp;
	}

	@Override
	public int getStatus() {
		return resp.getStatus();
	}

	@Override
	public MultivaluedMap<String, Object> getHeaders() {
		return resp.getHeaders();
	}

	@Override
	public String getHeaderString(String name) {
		return resp.getHeaderString(name);
	}

	@Override
	public <T> T getEntity(Class<T> type) throws RQLException {
		if (resp.getStatus() >= 400) {
			throw new RQLException(getErrorMessage(resp));
		}
		return (T) (entityAsObject == null ? (entityAsObject = resp.readEntity(type)) : entityAsObject);
	}

	@Override
	public <T> List<T> getEntityAsList(Class<T> type) throws RQLException {
		if (resp.getStatus() >= 400) {
			throw new RQLException(getErrorMessage(resp));
		}
		return entityAsList == null ? (entityAsList = resp.readEntity(new GenericType<List<T>>() {
		})) : entityAsList;
	}

	@Override
	public Map<String, Object> getEntityAsMap() throws RQLException {
		if (resp.getStatus() >= 400) {
			throw new RQLException(getErrorMessage(resp));
		}
		return entityAsMap == null ? (entityAsMap = resp.readEntity(new GenericType<Map<String, Object>>() {
		})) : entityAsMap;
	}

	private String getErrorMessage(javax.ws.rs.core.Response resp) {
		return String.format("Query resource error, %s %s, status %s, response: %s", method, uri, resp.getStatus(), resp.readEntity(String.class));
	}

	@Override
	public List<Map<String, Object>> getEntityAsListMap() throws RQLException {
		if (resp.getStatus() >= 400) {
			throw new RQLException(getErrorMessage(resp));
		}
		return entityAsListMap == null
				? (entityAsListMap = resp.readEntity(new GenericType<List<Map<String, Object>>>() {
				})) : entityAsListMap;
	}

}

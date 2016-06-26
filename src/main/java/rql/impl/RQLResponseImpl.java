package rql.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import rql.RQLException;
import rql.RQLResponse;
import rql.Response;

class RQLResponseImpl implements RQLResponse {

	private List<Response> resps = new ArrayList<>(1);

	@Override
	public int getStatus() {
		return getStatus(0);
	}

	@Override
	public int getStatus(int index) {
		return resps.get(index).getStatus();
	}

	@Override
	public MultivaluedMap<String, Object> getHeaders() {
		return getHeaders(0);
	}

	@Override
	public MultivaluedMap<String, Object> getHeaders(int index) {
		return resps.get(index).getHeaders();
	}

	@Override
	public String getHeaderString(String name) {
		return getHeaderString(name, 0);
	}

	@Override
	public String getHeaderString(String name, int index) {
		return resps.get(index).getHeaderString(name);
	}

	@Override
	public <T> T getEntity(Class<T> type) throws RQLException {
		return getEntity(type, 0);
	}

	@Override
	public <T> T getEntity(Class<T> type, int index) throws RQLException {
		return resps.get(index).getEntity(type);
	}

	@Override
	public <T> List<T> getEntityAsList(Class<T> type) throws RQLException {
		return getEntityAsList(type, 0);
	}

	@Override
	public <T> List<T> getEntityAsList(Class<T> type, int index) throws RQLException {
		return resps.get(index).getEntityAsList(type);
	}

	void addResponse(Response resp) {
		resps.add(resp);
	}
}

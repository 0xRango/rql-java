package rql;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

public interface RQLResponse {
	public int getStatus();

	public int getStatus(int index);

	public MultivaluedMap<String, Object> getHeaders();

	public MultivaluedMap<String, Object> getHeaders(int index);

	public String getHeaderString(String name);

	public String getHeaderString(String name, int index);

	public <T> T getEntity(Class<T> type) throws RQLException;

	public <T> T getEntity(Class<T> type, int index) throws RQLException;

	public <T> List<T> getEntityAsList(Class<T> type) throws RQLException;

	public <T> List<T> getEntityAsList(Class<T> type, int index) throws RQLException;

}

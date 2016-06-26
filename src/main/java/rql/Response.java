package rql;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

public interface Response {

	int getStatus();

	MultivaluedMap<String, Object> getHeaders();

	String getHeaderString(String name);

	<T> T getEntity(Class<T> type) throws RQLException;

	<T> List<T> getEntityAsList(Class<T> type) throws RQLException;

	Map<String, Object> getEntityAsMap() throws RQLException;

	List<Map<String, Object>> getEntityAsListMap() throws RQLException;
}

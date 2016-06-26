package rql.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.beanutils.BeanUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import rql.RQLException;
import rql.Response;

class UnionResponse implements Response {

	private Map<SelectResponse, String> selects = new HashMap<SelectResponse, String>();

	@Override
	public int getStatus() {
		return 200;
	}

	@Override
	public MultivaluedMap<String, Object> getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHeaderString(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> getEntityAsList(Class<T> type) throws RQLException{
		throw new RuntimeException("Union Response does not support GenericType entity");
	}

	@Override
	public <T> T getEntity(Class<T> type) throws RQLException{
		Map<String, Object> entity = getEntityAsMap();
		if (type == String.class) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				long start = System.currentTimeMillis();
				T result = (T) mapper.writeValueAsString(entity);
				System.out.println("Convert ellapsed: " + (System.currentTimeMillis() - start));
				return result;
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			Object result = type.newInstance();
			BeanUtils.populate(result, entity);
			return (T) result;
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public Map<String, Object> getEntityAsMap() throws RQLException{
		Map<String, Object> map = new HashMap<>();
		for (SelectResponse resp : this.selects.keySet()) {
			if (this.selects.get(resp) != null) {
				map.put(this.selects.get(resp), resp.getEntityAsMap());
			} else {
				map.putAll(resp.getEntityAsMap());
			}
		}
		return map;
	}

	@Override
	public List<Map<String, Object>> getEntityAsListMap() throws RQLException{
		throw new RuntimeException("Union Response does not support List entity");
	}

	void addResponse(String alias, SelectResponse resp) {
		this.selects.put(resp, alias);
	}

}

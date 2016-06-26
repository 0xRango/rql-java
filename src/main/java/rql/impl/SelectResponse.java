package rql.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.beanutils.BeanUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import rql.RQLException;
import rql.Response;
import rql.impl.model.QueryField;

class SelectResponse implements Response {
	public static class Entry {
		private String key;
		private Object value;

		public Entry(String key, Object value) {
			this.setKey(key);
			this.setValue(value);
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}
	}

	private Response response;
	private List<QueryField> fields;
	private boolean selectAll;

	public boolean isSelectAll() {
		return selectAll;
	}

	public void setSelectAll(boolean selectAll) {
		this.selectAll = selectAll;
	}

	private String resultAlias;

	public SelectResponse() {
	}

	public SelectResponse(Response resp) {
		this.response = resp;
	}

	public void setFields(List<QueryField> fields) {
		if (fields.size() == 0 || (fields.size() == 1 && fields.get(0).isAll()))
			this.selectAll = true;
		this.fields = fields;
	}

	public void addQueryField(QueryField field) {
		if (field.isAll())
			this.selectAll = true;
		if (this.fields == null)
			this.fields = new ArrayList<QueryField>();
		this.fields.add(field);
	}

	public List<QueryField> getFields() {
		return fields;
	}

	public String getResultAlias() {
		return resultAlias;
	}

	public void setResultAlias(String resultAlias) {
		this.resultAlias = resultAlias;
	}

	@Override
	public String getHeaderString(String name) {
		return response.getHeaderString(name);
	}

	@Override
	public MultivaluedMap<String, Object> getHeaders() {
		return response.getHeaders();
	}

	@Override
	public int getStatus() {
		return response.getStatus();
	}

	@Override
	public <T> List<T> getEntityAsList(Class<T> type) throws RQLException {
		List entity = response.getEntityAsList(HashMap.class);
		return (List<T>) entity.stream().map(i -> convertEntity(i, type)).collect(Collectors.toList());
	}

	private <T> T convertEntity(Object entity, Class<T> type) {
		if (type.isAssignableFrom(entity.getClass()))
			return (T) entity;

		// do object mapping

		if (type == String.class) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				T result = (T) mapper.writeValueAsString(entity);
				return result;
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			Object result = type.newInstance();
			final Map<String, Object> value = (Map<String, Object>) entity;
			BeanUtils.populate(result, value);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return (T) entity;
	}

	@Override
	public <T> T getEntity(Class<T> type) throws RQLException {
		if (selectAll) {
			return response.getEntity(type);
		}
		return convertEntity(getEntityAsMap(), type);
	}

	@Override
	public Map<String, Object> getEntityAsMap() throws RQLException {
		Map<String, Object> result = response.getEntityAsMap();
		if (!selectAll) {
			return fields.stream().filter(field -> {
				return result.containsKey(field.getField());
			}).map(field -> {
				return new Entry(field.getAlias() != null ? field.getAlias() : field.getField(),
						result.get(field.getField()));
			}).collect(Collectors.toMap(e -> {
				return e.getKey();
			}, e -> {
				return e.getValue();
			}));
		}
		return result;
	}

	@Override
	public List<Map<String, Object>> getEntityAsListMap() throws RQLException {
		List<Map<String, Object>> result = response.getEntityAsListMap();
		if (!selectAll) {
			return result.stream().map(row -> {
				return fields.stream().filter(field -> {
					return row.containsKey(field.getField());
				}).map(field -> {
					return new Entry(field.getAlias() != null ? field.getAlias() : field.getField(),
							row.get(field.getField()));
				}).collect(Collectors.toMap(e -> {
					return e.getKey();
				}, e -> {
					return e.getValue();
				}));
			}).collect(Collectors.toList());
		}
		return result;
	}

}

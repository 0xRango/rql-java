package rql.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.beanutils.BeanUtils;

import rql.RQLException;
import rql.Response;
import rql.impl.SelectResponse.Entry;
import rql.impl.model.QueryField;

class JoinResponse implements Response {

	private Map<String, List<? extends Map>> selects = new HashMap<>();
	private String primaryAlias;
	private SelectResponse primaryResponse;

	public JoinResponse() {
	}

	public void setPrimaryResponse(String alias, SelectResponse primaryResponse) {
		this.primaryAlias = alias;
		this.primaryResponse = primaryResponse;
		this.primaryResponse.setSelectAll(true);
	}

	@Override
	public int getStatus() {
		return primaryResponse.getStatus();
	}

	@Override
	public MultivaluedMap<String, Object> getHeaders() {
		return primaryResponse.getHeaders();
	}

	@Override
	public String getHeaderString(String name) {
		return primaryResponse.getHeaderString(name);
	}

	@Override
	public <T> List<T> getEntityAsList(Class<T> type) throws RQLException {
		List<Map<String, Object>> mergedResult = getEntityAsListMap();
		return mergedResult.stream().map(map -> {
			T result = null;
			try {
				result = type.newInstance();
				BeanUtils.populate(result, map);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return result;
		}).collect(Collectors.toList());
	}

	@Override
	public <T> T getEntity(Class<T> type) throws RQLException {
		throw new RuntimeException("Join Response only support GenericType Collection entity");
	}

	@Override
	public Map<String, Object> getEntityAsMap() throws RQLException {
		throw new RuntimeException("Join Response only support GenericType Collection entity");
	}

	@Override
	public List<Map<String, Object>> getEntityAsListMap() throws RQLException {
		List<? extends Map<String, Object>> primaryResult = this.primaryResponse.getEntityAsListMap();

		List<Map<String, Object>> mergedResult = new ArrayList<>();
		for (int i = 0; i < primaryResult.size(); i++) {
			Map<String, Object> primaryEntry = primaryResult.get(i);

			Map<String, Object> filteredValue = new HashMap<String, Object>();
			for (QueryField field : primaryResponse.getFields()) {
				if ("*".equals(field.getField())) {
					filteredValue.putAll(primaryEntry);
					break;
				}
			}

			primaryResponse.getFields().stream().filter(field -> {
				return this.primaryAlias.equals(field.getResource()) && primaryEntry.containsKey(field.getField());
			}).forEach(field -> {
				filteredValue.put(field.getAlias() != null ? field.getAlias() : field.getField(),
						primaryEntry.get(field.getField()));
			});

			for (String alias : selects.keySet()) {
				Map<String, Object> join = selects.get(alias).get(i);

				Map<String, Object> joinFilteredValue = primaryResponse.getFields().stream().filter(field -> {
					return alias.equals(field.getResource()) && join.containsKey(field.getField());
				}).map(field -> {
					return new Entry(field.getAlias() != null ? field.getAlias() : field.getField(),
							join.get(field.getField()));
				}).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
				filteredValue.putAll(joinFilteredValue);
			}
			mergedResult.add(filteredValue);
		}
		return mergedResult;
	}

	void addResponse(String alias, List<? extends Map> list) {
		this.selects.put(alias, list);
	}

}

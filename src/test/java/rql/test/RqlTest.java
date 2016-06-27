package rql.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Assert;
import org.junit.Test;

import rql.RQLClient;
import rql.RQLException;
import rql.RQLResponse;
import rql.Statement;
import rql.test.RqlTest.TestResource.Domain;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class RqlTest extends JerseyTest {

	@Path("")
	public static class TestResource {

		public static class Domain {
			public int id = 1;
			public String name = "root";
		}

		@GET
		@Path("simple-object")
		public Domain simpleObject() {
			return new Domain();
		}

		@GET
		@Path("simple-map")
		public Map simpleMap() {
			Map map = new HashMap();
			map.put("a", "a");
			map.put("b", "b");
			return map;
		}

		@GET
		@Path("simple-string")
		public String simpleString() {
			return "hello";
		}

		@GET
		@Path("name")
		public Map queryName(@QueryParam("id") String id) {
			// try {
			// Thread.sleep(35000);
			// } catch (InterruptedException e) {
			// }
			Map map = new HashMap();
			map.put(id, id);
			map.put("name", id + "'s Name");
			return map;
		}

		@GET
		@Path("names")
		public List queryNames(@QueryParam("id") List<String> ids) {
			List result = new ArrayList();
			for (String id : ids) {
				Map map = new HashMap();
				map.put(id, id);
				map.put("name", id + "'s Name");
				result.add(map);
			}
			return result;
		}

		@GET
		@Path("header-test")
		public String headerEcho(@HeaderParam("header-str") String header) {
			return header;
		}

		@GET
		@Path("simple-list")
		@Produces(MediaType.APPLICATION_JSON)
		public Response simpleList() {
			Map a = new HashMap();
			a.put("a", "a1");
			a.put("b", "b1");
			Map b = new HashMap();
			b.put("a", "a2");
			b.put("b", "b2");
			Response response = Response.status(200).entity(Arrays.asList(a, b)).header("X-total-count", "2").build();
			return response;

		}

		@POST
		@Consumes("application/json")
		@Path("test-post")
		public Map testPost(Map params) {
			Map a = new HashMap();
			a.put("a", params.get("a"));
			return a;

		}
	}

	private RQLClient client = RQLClient.getDefault();

	@Override
	protected Application configure() {
		forceSet(TestProperties.CONTAINER_PORT, "8080");
		forceSet("jersey.config.server.tracing.type", "ALL");
		forceSet("jersey.config.server.tracing.threshold", "TRACE");
		enable(TestProperties.LOG_TRAFFIC);
		enable(TestProperties.DUMP_ENTITY);

		return new ResourceConfig(TestResource.class);
	}

	private RQLResponse exec(String rql) throws RQLException {
		Statement stmt = client.createStatement(rql);
		return stmt.execute();
	}

	@Test
	public void testSimpleObject() throws RQLException {
		String rql = "select id, name from <GET http://127.0.0.1:8080/simple-object>";
		RQLResponse resp = exec(rql);
		Domain domain = resp.getEntity(Domain.class);
		Assert.assertEquals(1, domain.id);
		Assert.assertEquals("root", domain.name);
	}

	@Test
	public void testSimpleMap() throws RQLException {
		String rql = "select * from <GET http://127.0.0.1:8080/simple-map>";
		RQLResponse resp = exec(rql);
		Map result = resp.getEntity(Map.class);
		Assert.assertEquals("a", result.get("a"));
		Assert.assertEquals("b", result.get("b"));

		rql = "select a from <GET http://127.0.0.1:8080/simple-map>";
		resp = exec(rql);
		result = resp.getEntity(Map.class);
		Assert.assertEquals("a", result.get("a"));

		rql = "select a as b from <GET http://127.0.0.1:8080/simple-map>";
		resp = exec(rql);
		result = resp.getEntity(Map.class);
		Assert.assertEquals("a", result.get("b"));
		Assert.assertNull(result.get("a"));
	}

	@Test
	public void testMultipleStatements() throws RQLException {
		String rql = "select * from <GET http://127.0.0.1:8080/simple-map>;\n"
				+ "select * from <GET http://127.0.0.1:8080/simple-map>";
		RQLResponse resp = exec(rql);
		Map result = resp.getEntity(Map.class);
		Assert.assertEquals("a", result.get("a"));
		Assert.assertEquals("b", result.get("b"));
		result = resp.getEntity(Map.class, 1);
		Assert.assertEquals("a", result.get("a"));
		Assert.assertEquals("b", result.get("b"));
	}

	@Test
	public void testResourceAlias() throws RQLException {
		String rql = "Resource a: <GET http://127.0.0.1:8080/simple-string>\n" + "Select * from a";
		RQLResponse resp = exec(rql);
		Assert.assertEquals("hello", resp.getEntity(String.class));
	}

	@Test
	public void testHeader() throws RQLException {
		String rql = "Resource a: <GET http://127.0.0.1:8080/header-test>\n" + "Header header-str: 'Header'"
				+ "Select * from a";
		RQLResponse resp = exec(rql);
		Assert.assertEquals("Header", resp.getEntity(String.class));

		rql = "Parameter header: 'Header-string'\n"
				+ "Select * from <GET http://127.0.0.1:8080/header-test Header header-str=:header>";
		resp = exec(rql);
		Assert.assertEquals("Header-string", resp.getEntity(String.class));

		rql = "Resource a: <GET http://127.0.0.1:8080/header-test Header header-str=:header>\n"
				+ "Parameter header: 'Header-string'\n" + "Select * from a";
		resp = exec(rql);
		Assert.assertEquals("Header-string", resp.getEntity(String.class));
	}

	@Test
	public void testSimpleString() throws RQLException {
		String rql = "select * from <GET http://127.0.0.1:8080/simple-string>";
		RQLResponse resp = exec(rql);
		Assert.assertEquals("hello", resp.getEntity(String.class));
	}

	@Test
	public void testSimpleList() throws RQLException {
		String rql = "select * from <GET http://127.0.0.1:8080/simple-list>";
		RQLResponse resp = exec(rql);
		List<HashMap> result = resp.getEntityAsList(HashMap.class);
		Assert.assertEquals(2, result.size());
		Assert.assertEquals("2", resp.getHeaderString("X-total-count"));
		Assert.assertEquals("a1", result.get(0).get("a"));
		Assert.assertEquals("a2", result.get(1).get("a"));
		Assert.assertEquals("b1", result.get(0).get("b"));
		Assert.assertEquals("b2", result.get(1).get("b"));
	}

	@Test
	public void unionTest() throws RQLException {
		String rql = "select * from <GET http://127.0.0.1:8080/simple-map> as r1\n"
				+ "union select a from <GET http://127.0.0.1:8080/simple-map> as r2\n"
				+ "union select a from <GET http://127.0.0.1:8080/simple-map>";
		RQLResponse resp = exec(rql);
		Map result = resp.getEntity(HashMap.class);
		Assert.assertEquals("a", ((Map) result.get("r1")).get("a"));
		Assert.assertEquals("a", ((Map) result.get("r2")).get("a"));
		Assert.assertNull(((Map) result.get("r2")).get("b"));
		Assert.assertEquals("a", result.get("a"));
		Assert.assertNull(result.get("b"));
	}

	@Test
	public void joinTest() throws RQLException {
		String rql = "select a.*, b.name from <GET http://127.0.0.1:8080/simple-list> as a\n"
				+ "join <GET http://127.0.0.1:8080/name> as b on b.id=a.a";
		RQLResponse resp = exec(rql);
		List<HashMap> result = resp.getEntityAsList(HashMap.class);
		Assert.assertEquals(2, result.size());
		Assert.assertEquals("2", resp.getHeaderString("X-total-count"));
		Assert.assertEquals("a1", result.get(0).get("a"));
		Assert.assertEquals("a1's Name", result.get(0).get("name"));

		rql = "select a.*, b.name from <GET http://127.0.0.1:8080/simple-list> as a\n"
				+ "join <GET http://127.0.0.1:8080/names> as b on b.id in a.a";
		resp = exec(rql);
		result = resp.getEntityAsList(HashMap.class);
		Assert.assertEquals(2, result.size());
		Assert.assertEquals("2", resp.getHeaderString("X-total-count"));
		Assert.assertEquals("a1", result.get(0).get("a"));
		Assert.assertEquals("a1's Name", result.get(0).get("name"));
	}


	@Test
	public void testPost() throws RQLException {
		String rql = "Parameter data: '{\"a\":\"a\"}'\n"
				+ "select * from <POST http://127.0.0.1:8080/test-post :data>";
		RQLResponse resp = exec(rql);
		Map result = resp.getEntity(HashMap.class);
		Assert.assertEquals("a", result.get("a"));
	}
}

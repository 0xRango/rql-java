package rql;

import rql.impl.StatementFactory;
import rql.impl.jersey.JerseyExecutor;

public class RQLClient {

	private static class DefaultClientHolder {
		public static RQLClient defaultClient = new RQLClient(new JerseyExecutor());
	}

	public static RQLClient getDefault() {
		return DefaultClientHolder.defaultClient;
	}

	private RequestExecutor executor;

	private StatementFactory factory = new StatementFactory();

	public RQLClient(RequestExecutor executor) {
		this.executor = executor;
	}

	public Statement createStatement(String rql) throws RQLException {
		return factory.createStatement(executor, rql);
	}

}

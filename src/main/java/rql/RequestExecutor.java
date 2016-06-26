package rql;

import rql.impl.StatementContext;
import rx.Observable;

public interface RequestExecutor {

	public Observable<Response> request(StatementContext context, Resource resource);

	public void requestAsync(StatementContext context, Resource resource);

}

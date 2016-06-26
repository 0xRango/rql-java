package rql;

public interface Statement {

	void addHeader(String name, String value);

	void addParameter(String name, String value);

	RQLResponse execute() throws RQLException;

}

package rql.impl;

import java.util.BitSet;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import rql.RQLException;
import rql.RequestExecutor;
import rql.Statement;
import rql.antlr.RQLLexer;
import rql.antlr.RQLParser;

public class StatementFactory {

	public Statement createStatement(RequestExecutor executor, String rql) throws RQLException {
		try {
			RQLLexer lexer = new RQLLexer(new ANTLRInputStream(rql));
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			RQLParser parser = new RQLParser(tokens);
			parser.setBuildParseTree(true);
			parser.setErrorHandler(new BailErrorStrategy());
			parser.addErrorListener(new ANTLRErrorListener() {

				@Override
				public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
						int charPositionInLine, String msg, RecognitionException e) {
					throw new ParseCancellationException(
							String.format("Syntax error at %s:%s, %s", line, charPositionInLine, msg));
				}

				@Override
				public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex,
						int prediction, ATNConfigSet configs) {
					// TODO Auto-generated method stub

				}

				@Override
				public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex,
						BitSet conflictingAlts, ATNConfigSet configs) {
					// TODO Auto-generated method stub

				}

				@Override
				public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact,
						BitSet ambigAlts, ATNConfigSet configs) {
					// TODO Auto-generated method stub

				}
			});
			ParseTree tree = parser.statements();
			StatementParser model = new StatementParser();
			ParseTreeWalker.DEFAULT.walk(model, tree);
			return new StatementImpl(executor, model.getStatements());
		} catch (RecognitionException e) {
			throw new RQLException(e);
		} catch (ParseCancellationException e) {
			throw new RQLException(e.getMessage());
		}

	}
}

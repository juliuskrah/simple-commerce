// Generated from /Users/julius.krah/Documents/github/simple-commerce/simple-commerce/app/src/main/antlr4/com/simplecommerce/search/SearchQuery.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SearchQueryParser}.
 */
public interface SearchQueryListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SearchQueryParser#query}.
	 * @param ctx the parse tree
	 */
	void enterQuery(SearchQueryParser.QueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchQueryParser#query}.
	 * @param ctx the parse tree
	 */
	void exitQuery(SearchQueryParser.QueryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code TermExpr}
	 * labeled alternative in {@link SearchQueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterTermExpr(SearchQueryParser.TermExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code TermExpr}
	 * labeled alternative in {@link SearchQueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitTermExpr(SearchQueryParser.TermExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AndExpr}
	 * labeled alternative in {@link SearchQueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAndExpr(SearchQueryParser.AndExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AndExpr}
	 * labeled alternative in {@link SearchQueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAndExpr(SearchQueryParser.AndExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code OrExpr}
	 * labeled alternative in {@link SearchQueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterOrExpr(SearchQueryParser.OrExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code OrExpr}
	 * labeled alternative in {@link SearchQueryParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitOrExpr(SearchQueryParser.OrExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FieldValueTerm}
	 * labeled alternative in {@link SearchQueryParser#term}.
	 * @param ctx the parse tree
	 */
	void enterFieldValueTerm(SearchQueryParser.FieldValueTermContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FieldValueTerm}
	 * labeled alternative in {@link SearchQueryParser#term}.
	 * @param ctx the parse tree
	 */
	void exitFieldValueTerm(SearchQueryParser.FieldValueTermContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FieldRangeTerm}
	 * labeled alternative in {@link SearchQueryParser#term}.
	 * @param ctx the parse tree
	 */
	void enterFieldRangeTerm(SearchQueryParser.FieldRangeTermContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FieldRangeTerm}
	 * labeled alternative in {@link SearchQueryParser#term}.
	 * @param ctx the parse tree
	 */
	void exitFieldRangeTerm(SearchQueryParser.FieldRangeTermContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FieldQuotedTerm}
	 * labeled alternative in {@link SearchQueryParser#term}.
	 * @param ctx the parse tree
	 */
	void enterFieldQuotedTerm(SearchQueryParser.FieldQuotedTermContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FieldQuotedTerm}
	 * labeled alternative in {@link SearchQueryParser#term}.
	 * @param ctx the parse tree
	 */
	void exitFieldQuotedTerm(SearchQueryParser.FieldQuotedTermContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ParenTerm}
	 * labeled alternative in {@link SearchQueryParser#term}.
	 * @param ctx the parse tree
	 */
	void enterParenTerm(SearchQueryParser.ParenTermContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ParenTerm}
	 * labeled alternative in {@link SearchQueryParser#term}.
	 * @param ctx the parse tree
	 */
	void exitParenTerm(SearchQueryParser.ParenTermContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NotTerm}
	 * labeled alternative in {@link SearchQueryParser#term}.
	 * @param ctx the parse tree
	 */
	void enterNotTerm(SearchQueryParser.NotTermContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NotTerm}
	 * labeled alternative in {@link SearchQueryParser#term}.
	 * @param ctx the parse tree
	 */
	void exitNotTerm(SearchQueryParser.NotTermContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchQueryParser#field}.
	 * @param ctx the parse tree
	 */
	void enterField(SearchQueryParser.FieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchQueryParser#field}.
	 * @param ctx the parse tree
	 */
	void exitField(SearchQueryParser.FieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchQueryParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(SearchQueryParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchQueryParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(SearchQueryParser.ValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchQueryParser#range}.
	 * @param ctx the parse tree
	 */
	void enterRange(SearchQueryParser.RangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchQueryParser#range}.
	 * @param ctx the parse tree
	 */
	void exitRange(SearchQueryParser.RangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SearchQueryParser#quotedString}.
	 * @param ctx the parse tree
	 */
	void enterQuotedString(SearchQueryParser.QuotedStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link SearchQueryParser#quotedString}.
	 * @param ctx the parse tree
	 */
	void exitQuotedString(SearchQueryParser.QuotedStringContext ctx);
}
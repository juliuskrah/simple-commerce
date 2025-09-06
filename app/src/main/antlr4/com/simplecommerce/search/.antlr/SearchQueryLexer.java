// Generated from /Users/julius.krah/Documents/github/simple-commerce/simple-commerce/app/src/main/antlr4/com/simplecommerce/search/SearchQuery.g4 by ANTLR 4.13.1

package com.simplecommerce.search.generated;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class SearchQueryLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, OR=5, AND=6, NOT=7, IDENTIFIER=8, NUMBER=9, 
		BOOLEAN=10, QUOTED_STRING=11, WS=12, UNKNOWN=13;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "OR", "AND", "NOT", "IDENTIFIER", "NUMBER", 
			"BOOLEAN", "QUOTED_STRING", "WS", "UNKNOWN"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "':'", "'('", "')'", "'..'", "'OR'", "'AND'", "'NOT'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, "OR", "AND", "NOT", "IDENTIFIER", "NUMBER", 
			"BOOLEAN", "QUOTED_STRING", "WS", "UNKNOWN"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public SearchQueryLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "SearchQuery.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000\rb\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
		"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
		"\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007"+
		"\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b"+
		"\u0007\u000b\u0002\f\u0007\f\u0001\u0000\u0001\u0000\u0001\u0001\u0001"+
		"\u0001\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001"+
		"\u0007\u0005\u00072\b\u0007\n\u0007\f\u00075\t\u0007\u0001\b\u0004\b8"+
		"\b\b\u000b\b\f\b9\u0001\b\u0001\b\u0004\b>\b\b\u000b\b\f\b?\u0003\bB\b"+
		"\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0003\tM\b\t\u0001\n\u0001\n\u0005\nQ\b\n\n\n\f\nT\t\n\u0001\n\u0001"+
		"\n\u0001\u000b\u0004\u000bY\b\u000b\u000b\u000b\f\u000bZ\u0001\u000b\u0001"+
		"\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0000\u0000\r\u0001\u0001\u0003"+
		"\u0002\u0005\u0003\u0007\u0004\t\u0005\u000b\u0006\r\u0007\u000f\b\u0011"+
		"\t\u0013\n\u0015\u000b\u0017\f\u0019\r\u0001\u0000\u0005\u0003\u0000A"+
		"Z__az\u0004\u000009AZ__az\u0001\u000009\u0001\u0000\"\"\u0003\u0000\t"+
		"\n\r\r  h\u0000\u0001\u0001\u0000\u0000\u0000\u0000\u0003\u0001\u0000"+
		"\u0000\u0000\u0000\u0005\u0001\u0000\u0000\u0000\u0000\u0007\u0001\u0000"+
		"\u0000\u0000\u0000\t\u0001\u0000\u0000\u0000\u0000\u000b\u0001\u0000\u0000"+
		"\u0000\u0000\r\u0001\u0000\u0000\u0000\u0000\u000f\u0001\u0000\u0000\u0000"+
		"\u0000\u0011\u0001\u0000\u0000\u0000\u0000\u0013\u0001\u0000\u0000\u0000"+
		"\u0000\u0015\u0001\u0000\u0000\u0000\u0000\u0017\u0001\u0000\u0000\u0000"+
		"\u0000\u0019\u0001\u0000\u0000\u0000\u0001\u001b\u0001\u0000\u0000\u0000"+
		"\u0003\u001d\u0001\u0000\u0000\u0000\u0005\u001f\u0001\u0000\u0000\u0000"+
		"\u0007!\u0001\u0000\u0000\u0000\t$\u0001\u0000\u0000\u0000\u000b\'\u0001"+
		"\u0000\u0000\u0000\r+\u0001\u0000\u0000\u0000\u000f/\u0001\u0000\u0000"+
		"\u0000\u00117\u0001\u0000\u0000\u0000\u0013L\u0001\u0000\u0000\u0000\u0015"+
		"N\u0001\u0000\u0000\u0000\u0017X\u0001\u0000\u0000\u0000\u0019^\u0001"+
		"\u0000\u0000\u0000\u001b\u001c\u0005:\u0000\u0000\u001c\u0002\u0001\u0000"+
		"\u0000\u0000\u001d\u001e\u0005(\u0000\u0000\u001e\u0004\u0001\u0000\u0000"+
		"\u0000\u001f \u0005)\u0000\u0000 \u0006\u0001\u0000\u0000\u0000!\"\u0005"+
		".\u0000\u0000\"#\u0005.\u0000\u0000#\b\u0001\u0000\u0000\u0000$%\u0005"+
		"O\u0000\u0000%&\u0005R\u0000\u0000&\n\u0001\u0000\u0000\u0000\'(\u0005"+
		"A\u0000\u0000()\u0005N\u0000\u0000)*\u0005D\u0000\u0000*\f\u0001\u0000"+
		"\u0000\u0000+,\u0005N\u0000\u0000,-\u0005O\u0000\u0000-.\u0005T\u0000"+
		"\u0000.\u000e\u0001\u0000\u0000\u0000/3\u0007\u0000\u0000\u000002\u0007"+
		"\u0001\u0000\u000010\u0001\u0000\u0000\u000025\u0001\u0000\u0000\u0000"+
		"31\u0001\u0000\u0000\u000034\u0001\u0000\u0000\u00004\u0010\u0001\u0000"+
		"\u0000\u000053\u0001\u0000\u0000\u000068\u0007\u0002\u0000\u000076\u0001"+
		"\u0000\u0000\u000089\u0001\u0000\u0000\u000097\u0001\u0000\u0000\u0000"+
		"9:\u0001\u0000\u0000\u0000:A\u0001\u0000\u0000\u0000;=\u0005.\u0000\u0000"+
		"<>\u0007\u0002\u0000\u0000=<\u0001\u0000\u0000\u0000>?\u0001\u0000\u0000"+
		"\u0000?=\u0001\u0000\u0000\u0000?@\u0001\u0000\u0000\u0000@B\u0001\u0000"+
		"\u0000\u0000A;\u0001\u0000\u0000\u0000AB\u0001\u0000\u0000\u0000B\u0012"+
		"\u0001\u0000\u0000\u0000CD\u0005t\u0000\u0000DE\u0005r\u0000\u0000EF\u0005"+
		"u\u0000\u0000FM\u0005e\u0000\u0000GH\u0005f\u0000\u0000HI\u0005a\u0000"+
		"\u0000IJ\u0005l\u0000\u0000JK\u0005s\u0000\u0000KM\u0005e\u0000\u0000"+
		"LC\u0001\u0000\u0000\u0000LG\u0001\u0000\u0000\u0000M\u0014\u0001\u0000"+
		"\u0000\u0000NR\u0005\"\u0000\u0000OQ\b\u0003\u0000\u0000PO\u0001\u0000"+
		"\u0000\u0000QT\u0001\u0000\u0000\u0000RP\u0001\u0000\u0000\u0000RS\u0001"+
		"\u0000\u0000\u0000SU\u0001\u0000\u0000\u0000TR\u0001\u0000\u0000\u0000"+
		"UV\u0005\"\u0000\u0000V\u0016\u0001\u0000\u0000\u0000WY\u0007\u0004\u0000"+
		"\u0000XW\u0001\u0000\u0000\u0000YZ\u0001\u0000\u0000\u0000ZX\u0001\u0000"+
		"\u0000\u0000Z[\u0001\u0000\u0000\u0000[\\\u0001\u0000\u0000\u0000\\]\u0006"+
		"\u000b\u0000\u0000]\u0018\u0001\u0000\u0000\u0000^_\t\u0000\u0000\u0000"+
		"_`\u0001\u0000\u0000\u0000`a\u0006\f\u0001\u0000a\u001a\u0001\u0000\u0000"+
		"\u0000\b\u000039?ALRZ\u0002\u0006\u0000\u0000\u0000\u0001\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
grammar SearchQuery;

@header {
package com.simplecommerce.product.search.generated;
}

// Entry point
query: expr EOF;

// Expressions with precedence
expr
    : expr OR expr                          #OrExpr
    | expr AND expr                         #AndExpr
    | term                                  #TermExpr
    ;

term
    : field ':' value                       #FieldValueTerm
    | field ':' range                       #FieldRangeTerm
    | field ':' quotedString                #FieldQuotedTerm
    | '(' expr ')'                          #ParenTerm
    | NOT term                              #NotTerm
    ;

field: IDENTIFIER;

value: IDENTIFIER | NUMBER | BOOLEAN;

range: value '..' value;

quotedString: QUOTED_STRING;

// Keywords
OR: 'OR';
AND: 'AND';
NOT: 'NOT';

// Tokens
IDENTIFIER: [a-zA-Z_][a-zA-Z0-9_]*;
NUMBER: [0-9]+ ('.' [0-9]+)?;
BOOLEAN: 'true' | 'false';
QUOTED_STRING: '"' (~["])* '"';

// Whitespace
WS: [ \t\r\n]+ -> skip;

// Skip unrecognized characters (for better error handling)
UNKNOWN: . -> channel(HIDDEN);
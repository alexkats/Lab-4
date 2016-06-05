lexer grammar LexerForParser;

START_RULE : 'start' ;
RETURNS : 'returns' ;
MYPARSER : 'myparser' ;
HEADER : 'header' -> pushMode(ruleDeclaration) ;
SEMICOLON : ';' ;
RULE : [a-z][a-zA-Z0-9_]* ;
TOKEN : [A-Z][a-zA-Z0-9_]* ;
TWOCOLON : '::' -> pushMode(ruleDeclaration) ;
BEGIN_ATTRIBUTES : '[' -> pushMode(attributes), type(LEFT_SQUARE_BRACKET) ;

WS : [ \t\r\n]+ -> skip ;
COMMENT : '//' ~'\n'* '\n' -> skip ;
MULTILINE_COMMENT : '/*' .*? '*/' -> skip ;

mode ruleDeclaration;

RULE_OR : '|' ;
RULE_SEMICOLON : ';' -> popMode, type(SEMICOLON) ;
RULE_RULE : [a-z][a-zA-Z0-9_]* -> type(RULE) ;
RULE_TOKEN : [A-Z][a-zA-Z0-9_]* -> type(TOKEN) ;
CODE_IN_BRACKETS : LEFT_FIGURE_BRACKET CODE RIGHT_FIGURE_BRACKET ;
IN_SQUARE : '[' IN_SQUARE_BRACKETS ']' ;

RULE_WS : [ \t\r\n]+ -> skip ;
RULE_COMMENT : '//' ~'\n'* '\n' -> skip ;
RULE_MULTILINE_COMMENT : '/*' .*? '*/' -> skip ;

fragment IN_SQUARE_BRACKETS : TEXT+ IN_SQUARE IN_SQUARE_BRACKETS | TEXT+ ;
fragment STRING_VALUE : '"' (ESCAPE_SEQUENCE | ~["\\])* '"' ;
fragment CHAR_VALUE : '\'' (ESCAPE_SEQUENCE | ~['\\]) '\'' ;
fragment ESCAPE_SEQUENCE : '\\' (["\\/bnfrt]) ;
fragment TEXT : (~['"[\]])+ | STRING_VALUE | CHAR_VALUE ;

mode attributes;

LEFT_SQUARE_BRACKET : '[' -> pushMode(attributes) ;
RIGHT_SQUARE_BRACKET : ']' -> popMode ;
LEFT_ANGLE_BRACKET : '<' ;
RIGHT_ANGLE_BRACKET : '>' ;
COMMA : ',' ;
VALID_ID : [a-zA-Z_][a-zA-Z0-9_]* ;
ATTRIBUTES_WS : [ \t\r\n]+ -> skip ;

fragment LEFT_FIGURE_BRACKET : '{' ;
fragment RIGHT_FIGURE_BRACKET : '}' ;
fragment ESCAPE_LEFT_FIGURE_BRACKET : '\\{' ;
fragment ESCAPE_RIGHT_FIGURE_BRACKET : '\\}' ;
fragment CODE : CODE_TEXT CODE_IN_BRACKETS CODE | CODE_TEXT ;
fragment CODE_TEXT : (~[{}] | ESCAPE_LEFT_FIGURE_BRACKET | ESCAPE_RIGHT_FIGURE_BRACKET)* ;
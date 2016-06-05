lexer grammar LexerForLexer;

TOKEN : [A-Z][a-ZA-Z0-9_]* ;
TWOCOLON : '::' ;
SEMICOLON : ';' ;
TWOHASH : '##' ;
EXPRESSION : '\'' (~'\'')+ '\'' ;
SKIPPER : 'skip' ;
COMMA : ',' ;

WS : [ \t\r\n]+ -> skip ;
COMMENT : '//' ~'\n'* '\n' -> skip ;
MULTILINE_COMMENT : '/*' .*? '*/' -> skip ;
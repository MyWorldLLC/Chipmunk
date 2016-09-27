grammar Chipmunk;

@header {
	package chipmunk.compiler.parser;
}

WS : [ \t\r\n\u000C]+ -> skip;
NAME : [a-zA-Z_] [a-zA-Z0-9_]*;

// Special characters
LPAREN				: '(';
RPAREN				: ')';
LBRACK				: '[';
RBRACK				: ']';
LBRACE				: '{';
RBRACE				: '}';
COMMA				: ',';
DOT					: '.';
COLON				: ':';

// Operators
ADD 				: '+';
SUB 				: '-';
INC 				: '++';
DEC 				: '--';
MUL 				: '*';
DIV 				: '/';
FDIV 				: '//';
POW 				: '**';
MOD 				: '%';
XOR 				: '^';
ASSIGN 				: '=';
BNOT 				: '~';
NOT 				: '!';
GT 					: '>';
LT 					: '<';
GE 					: '>=';
LE 					: '<=';
EQ 					: '==';
NE 					: '!=';
BOR 				: '|';
BAND 				: '&';
OR 					: '||';
AND 				: '&&';
LSHIFT 				: '<<';
RSHIFT 				: '>>';
URSHIFT 			: '>>>';
ADD_ASSIGN 			: '+=';
SUB_ASSIGN 			: '-=';
MUL_ASSIGN 			: '*=';
DIV_ASSIGN 			: '/=';
FDIV_ASSIGN 		: '//=';
BAND_ASSIGN 		: '&=';
BOR_ASSIGN 			: '|=';
XOR_ASSIGN 			: '^=';
BNOT_ASSIGN 		: '~=';
MOD_ASSIGN 			: '%=';
LSHIFT_ASSIGN 		: '<<=';
RSHIFT_ASSIGN 		: '>>=';
URSHIFT_ASSIGN 		: '>>>=';

// Keywords
MODULE		: 'module';
FROM		: 'from';
IMPORT		: 'import';
SELF		: 'self';
CLASS		: 'class';
DEF			: 'def';
SHARED		: 'shared';
IN			: 'in';
IS			: 'is';
FOR			: 'for';
WHILE		: 'while';
BREAK		: 'break';
CONTINUE	: 'continue';
IF			: 'if';
ELSE		: 'else';
RETURN		: 'return';
TRY			: 'try';
CATCH		: 'catch';
THROW		: 'throw';



comment : '#' ~[\r\n]*;
moduleDec : MODULE NAME (DOT NAME)*;
varDec : NAME (COMMA NAME)* | NAME (COMMA NAME)* ASSIGN expression (COMMA expression)*;
expression : 'todo';
script : 'foo';
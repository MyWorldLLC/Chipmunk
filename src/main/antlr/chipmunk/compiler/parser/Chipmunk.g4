grammar Chipmunk;

@header {
	package chipmunk.compiler.parser;
}

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
ASSIGN 				: '=';
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
AS			: 'as';
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

// TODO - float and string literals
INTLITERAL	: (ADD|SUB)? [0-9_]+;
HEXLITERAL	: ('0x'|'0X') [0-9a-fA-F_]+;
OCTLITERAL	: ('0o'|'0O') [0-7_]+;
BINLITERAL	: ('0b'|'0B') [01_]+;

WS : [ \t\r\n\u000C]+ -> skip;

// Keep this below the keyword declarations - 
// placing it down here makes sure that
// keywords are recognized before names are
// recognized. If this rule was defined first,
// keywords would never be matched because all
// of them are valid names
NAME : [a-zA-Z_] [a-zA-Z0-9_]*;

COMMENT 	: '#' ~[\r\n]* -> skip
			;
		
unaryOp 	: (ADD | SUB | NOT | BNOT | INC | DEC)
			;
		
binaryOp 	: (ADD | SUB | MUL | DIV | FDIV | POW | MOD | XOR 
			| GT | LT | GE | LE | EQ | NE | BOR | BAND | OR
			| AND | LSHIFT | RSHIFT | URSHIFT)
			;
			
assign 		: (ASSIGN | ADD_ASSIGN | SUB_ASSIGN | MUL_ASSIGN
			| DIV_ASSIGN | FDIV_ASSIGN | BAND_ASSIGN | BOR_ASSIGN
			| XOR_ASSIGN | BNOT_ASSIGN | MOD_ASSIGN | LSHIFT_ASSIGN
			| RSHIFT_ASSIGN | URSHIFT_ASSIGN)
			;

moduleID 	: NAME (DOT NAME)*
		 	;

nameList 	: NAME (COMMA NAME)*
		 	;


importStatement : IMPORT moduleID (AS NAME)?
			 	| FROM moduleID IMPORT (MUL | nameList (AS nameList)?)
			 	;
			 
tuple : LPAREN (expressionList)? RPAREN
	  ;
	  
dict  : LBRACE (expression COLON expression (COMMA expression COLON expression)* )? RBRACE
	  ;
	  
list  : LBRACK expressionList? RBRACK
	  ;

expression 	: NAME
			| unaryOp expression
			| expression unaryOp
			| expression binaryOp expression
			| LPAREN expression RPAREN
			| tuple
			| dict
			| list
		   	;
		   	
expressionList : expression (COMMA expression)*
			   ;
			   
assignment : expression assign expression
		   ;
		   
assignAt : expression LBRACK expression RBRACK ASSIGN expression
		 ;
	  
statement : importStatement
		  | NAME
		  | assignment
		  | assignAt
		  ;
	  
module : statement*;
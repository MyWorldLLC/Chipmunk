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
TRUE		: 'true';
FALSE		: 'false';
NULL		: 'null';

// TODO - string literals
INTLITERAL	: [0-9_]+;
HEXLITERAL	: ('0x'|'0X') [0-9a-fA-F_]+;
OCTLITERAL	: ('0o'|'0O') [0-7_]+;
BINLITERAL	: ('0b'|'0B') [01_]+;

FLOATLITERAL : INTLITERAL? '.' INTLITERAL ([eE] (SUB)? INTLITERAL)?
			 | INTLITERAL [eE] (SUB)? INTLITERAL
			 ;

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
			
powOp       : POW
            ;
			
multOp      : MUL
            | DIV
            | FDIV
            | MOD
            ;
            
addOp       : ADD
            | SUB
            ;
            
shiftOp     : LSHIFT
            | RSHIFT
            ;
            
bAnd        : BAND
            ;
            
bXor        : XOR
            ;
            
bOr         : BOR
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
			 	
// helper parser rules so that we don't
// need to figure out what type of literal we got
intliteral	: INTLITERAL;
hexliteral	: HEXLITERAL;
octliteral	: OCTLITERAL;
binliteral	: BINLITERAL;
boolliteral : TRUE | FALSE;
floatliteral: FLOATLITERAL;

literal : intliteral
		| hexliteral
		| octliteral
		| binliteral
		| boolliteral
		| floatliteral
		;

name : NAME
     ;
		
call : expression LPAREN expressionList? RPAREN
	 ;
			 
tuple : LPAREN (expressionList)? RPAREN
	  ;
	  
dict  : LBRACE (expression COLON expression (COMMA expression COLON expression)* )? RBRACE
	  ;
	  
list  : LBRACK expressionList? RBRACK
	  ;

expression 	: expression DOT expression
			| unaryOp expression
			| expression unaryOp
			| expression binaryOp expression
			| LPAREN expression RPAREN
			| tuple
			| dict
			| list
			| literal
			| name
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
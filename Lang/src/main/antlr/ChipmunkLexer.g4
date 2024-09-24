lexer grammar ChipmunkLexer;

channels { WS_CHANNEL, NL_CHANNEL }

WS: [ t]+ -> channel(WS_CHANNEL);
NL: ('rn' | 'r' | 'n') -> channel(NL_CHANNEL);

COMMENT: '#' . NL;

BINARYLITERAL: ('0b' | '0B')[01_]+;
OCTLITERAL: ('0o' |' 0O' )[0-7_]+;
HEXLITERAL: ('0x'|'0X')[a-fA-F0-9_]+;
FLOATLITERAL: [0-9]* '.'? [0-9]+ (('e'|'E')'-'? [0-9]+)?;
INTLITERAL: [0-9][0-9_]*;
BOOLLITERAL: 'true' | 'false';

// TODO - probably need to move strings to the parser so we can handle interpolation
STRINGLITERAL: ('"' ('\\"' | [^"])* '"') | ('\'' ('\\\'' | [^'])* '\'');

LBRACE: '{';
RBRACE: '}';
LBRACKET: '[';
RBRACKET: ']';
LPAREN: '(';
RPAREN: ')';
COMMA: ',';

DOUBLECOLON: '::';
COLON: ':';
DOUBLEEQUALS: '==';
EQUALS: '=';
DOUBLEDOTLESS: '..<';
DOUBLEDOT: '..';
DOT: '.';
DOUBLESTAR: '**';
STAR: '*';
DOUBLEPLUS: '++';
PLUS: '+';
DOUBLEMINUS: '--';
MINUS: '-';
DOUBLEFSLASH: '//';
FSLASH: '/';
DOUBLEBAR: '||';
BAR: '|';
EXCLAMATION: '!';
TILDE: '~';
CARET: '^';
DOUBLELESSTHAN: '<<';
LESSTHAN: '<';
TRIPLEMORETHAN: '>>>';
DOUBLEMORETHAN: '>>';
MORETHAN: '>';
PERCENT: '%';
DOUBLEAMPERSAND: '&&';
AMPERSAND: '&';

MODULE: 'module';
FROM: 'from';
IMPORT: 'import';
AS: 'as';
IN: 'in';
CLASS: 'class';
SHARED: 'shared';
NULL: 'null';
IF: 'if';
ELSE: 'else';
FOR: 'for';
WHILE: 'while';
BREAK: 'break';
CONTINUE: 'continue';
RETURN: 'return';
TRY: 'try';
CATCH: 'catch';
FINALLY: 'finally';
THROW: 'throw';
DEF: 'def';
VAR: 'var';
TRAIT: 'trait';
FINAL: 'final';
INSTANCEOF: 'instanceof';
IS: 'is';
MATCH: 'match';
CASE: 'case';
WHEN: 'when';

// identifiers go second to last so that they don't interfere with matching keywords
IDENTIFIER: [a-zA-Z_][a-zA-Z0-9_]*;

// Antlr automatically gives us EOF, but we also want a catch-all that we can use to try to keep
// lexing/parsing past mistakes (for error analysis, tooling, etc.).
UNRECOGNIZED: .;
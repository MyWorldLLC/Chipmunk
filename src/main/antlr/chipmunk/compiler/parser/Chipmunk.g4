grammar Chipmunk;

@header {
	package chipmunk.compiler.parser;
}


NL : [\n|\r\n];
WS : [ \t]+ -> skip;

comment : '#' ~NL*;
script : 'foo';
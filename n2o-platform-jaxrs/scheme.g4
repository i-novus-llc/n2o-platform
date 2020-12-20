grammar scheme;

file: expression* EOF;

expression: LPAREN expression* RPAREN | atom | QUOTATION expression;

atom: (PLUS | MINUS)? SCIENTIFIC_NUMBER | IDENTIFIER;

IDENTIFIER: VALID_ID_START VALID_ID_CHAR*;
QUOTATION: '`';

fragment VALID_ID_START:
    ('a' .. 'z') |
    ('A' .. 'Z') |
    '_';

fragment VALID_ID_CHAR: VALID_ID_START | ('0' .. '9');

//The NUMBER part gets its potential sign from "(PLUS | MINUS)* atom" in the expression rule
SCIENTIFIC_NUMBER: NUMBER (E SIGN? UNSIGNED_INTEGER)?;

fragment NUMBER: ('0' .. '9') + ('.' ('0' .. '9') +)?;
fragment UNSIGNED_INTEGER: ('0' .. '9')+;
fragment E: 'E' | 'e';
fragment SIGN: ('+' | '-');

LPAREN: '(';
RPAREN: ')';
PLUS: '+';
MINUS: '-';
TIMES: '*';
DIV: '/';
GT: '>';
LT : '<';
EQ : '=';
POINT : '.';
POW : '^';
WS : [ \r\n\t] + -> skip;
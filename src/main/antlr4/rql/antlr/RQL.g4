grammar RQL;

/* parser rules */
statements
    : statement (';' statement)*?
    ;

statement
    : (resource_alias | header | parameter)* (query | send)?
    ;

resource_alias
    : Resource ID ':' resource
    ;

header
    : Header ID ':' VALUE ;

parameter
    : Parameter ID ':' VALUE ;

resource
    : '<' method uri (header_expr)? (data_expr|body)? '>' ;

method
    : ID;

uri
    : URI ;

body
    :  ':' ID
    ;

name
    : ID
    ;

resource_name
    : ID
    ;

variable
    : (':'|'$') ID
    | VALUE
    ;

header_expr
    : Header name '=' variable (',' name '=' variable)*
    ;

data_expr
    : Data name '=' variable (',' name '=' variable)*
    ;

query
    : subquery | union | join | select
    ;

subquery
    : Select fields From '(' query ')'
    ;

select
    : Select fields From (resource | resource_name) (alias_name)?
    ;

fields
    : '*'
    | field_name (',' field_name)*
    ;

field_name
    : ID
    | FIELD_FULL
    | FIELD_WILD
    | (ID|FIELD_FULL) alias_name
    ;

alias_name
    : As name
    ;

union
    : (select|subquery) (Union (select|subquery))+
    ;

join
    : (select|subquery) (Join join_on)+
    ;

join_on
    : (resource | resource_name) alias_name On condition
    ;

condition
    : left operand right (',' left operand right)*
    ;

operand
    : EQ
    | IN
    ;

left
    : FIELD_FULL
    ;

right
    : FIELD_FULL
    ;

send
    : Send (resource | resource_name)
    ;


/* lexer rules */
ID  : [a-zA-Z][a-zA-Z_0-9-]*;

EQ  : '=' ;

VALUE
    : '\'' .*? '\'' ;

VARIABLE
    : ID | VALUE ;

FIELD_FULL
    : ID '.' ID
    ;

FIELD_WILD
    : ID '.*'
    ;

WS  : [ \t\n\r]+ -> skip ;

URI : ('http://'|'https://')[a-zA-Z0-9-._~:/?#\[\]{}@!$&()*+,;=]+ ;

Resource
    : R E S O U R C E [ \n];

Header
    : H E A D E R [ \n];

Parameter
    : P A R A M E T E R [ \n];

Data
    : D A T A [ \n];

Select
    : S E L E C T [ \n];

From
    : F R O M [ \n];

Join
    : J O I N [ \n];

On
    : O N [ \n];

As
    : A S [ \n];

Union
    : U N I O N [ \n];

Send
    : S E N D [ \n];

IN
    : I N [ \n];

fragment A: [aA];
fragment B: [bB];
fragment C: [cC];
fragment D: [dD];
fragment E: [eE];
fragment F: [fF];
fragment G: [gG];
fragment H: [hH];
fragment I: [iI];
fragment J: [jJ];
fragment K: [kK];
fragment L: [lL];
fragment M: [mM];
fragment N: [nN];
fragment O: [oO];
fragment P: [pP];
fragment Q: [qQ];
fragment R: [rR];
fragment S: [sS];
fragment T: [tT];
fragment U: [uU];
fragment V: [vV];
fragment W: [wW];
fragment X: [xX];
fragment Y: [yY];
fragment Z: [zZ];


grammar Fun;

file
    : block
    ;

block
    : statement*
    ;

blockWithBraces
    : LEFT_BRACE block RIGHT_BRACE
    ;

statement
    : functionDeclaration
    | variableDeclaration
    | expression
    | whileStatement
    | ifStatement
    | assignment
    | returnStatement
    ;

functionDeclaration
    : FUN identifier LEFT_PARENTHESIS parameterNames RIGHT_PARENTHESIS blockWithBraces
    ;

parameterNames
    : (identifier (COMMA identifier)*)?
    ;

variableDeclaration
    : VAR identifier (ASSIGN expression)?
    ;

whileStatement
    : WHILE LEFT_PARENTHESIS expression RIGHT_PARENTHESIS blockWithBraces
    ;

ifStatement
    : IF LEFT_PARENTHESIS expression RIGHT_PARENTHESIS blockWithBraces (ELSE blockWithBraces)?
    ;

assignment
    : identifier ASSIGN expression
    ;

returnStatement
    : RETURN expression
    ;

expression
    : orExpression
    ;

orExpression
    : andExpression                 # unaryOrExpression
    | orExpression OR andExpression # binaryOrExpression
    ;

andExpression
    : equalityExpression                   # unaryAndExpression
    | andExpression AND equalityExpression # binaryAndExpression
    ;

equalityExpression
    : relationalExpression                                    # unaryEqualityExpression
    | equalityExpression op = (EQ | NEQ) relationalExpression # binaryEqualityExpression
    ;

relationalExpression
    : additiveExpression                                               # unaryRelationalExpression
    | relationalExpression op = (GT | LT | GE | LE) additiveExpression # binaryRelationalExpression
    ;

additiveExpression
    : multiplicativeExpression                                        # unaryAdditiveExpression
    | additiveExpression op = (PLUS | MINUS) multiplicativeExpression # binaryAdditiveExpression
    ;

multiplicativeExpression
    : unaryExpression                                                 # unaryMultiplicativeExpression
    | multiplicativeExpression op = (MUL | DIV | MOD) unaryExpression # binaryMultiplicativeExpression
    ;

unaryExpression
    : functionCall
    | identifier
    | literal
    | LEFT_PARENTHESIS expression RIGHT_PARENTHESIS
    ;

functionCall
    : identifier LEFT_PARENTHESIS arguments RIGHT_PARENTHESIS
    ;

arguments
    : (expression (COMMA expression)*)?
    ;

FUN
    : 'fun'
    ;

VAR
    : 'var'
    ;

WHILE
    : 'while'
    ;

IF
    : 'if'
    ;

ELSE
    : 'else'
    ;

RETURN
    : 'return'
    ;

identifier
    : Identifier
    | InvalidIdentifier
    ;

Identifier
    : (Letter | UNDERSCORE) (Letter | Digit | UNDERSCORE)*
    ;

fragment
Letter
    : 'a'..'z'
    | 'A'..'Z'
    ;

literal
    : Number
    | LeadingZerosNumber
    ;

Number
    : MINUS? NonZeroDigit (Digit)*
    | '0'
    ;

fragment
Digit
    : '0'
    | NonZeroDigit
    ;

fragment
NonZeroDigit
    : '1'..'9'
    ;

LeadingZerosNumber
    : MINUS? '0' [0-9]+
    ;

InvalidIdentifier
    : [0-9]([0-9a-zA-Z])+
    ;

COMMA
    : ','
    ;

ASSIGN
    : '='
    ;

LEFT_BRACE
    : '{'
    ;

RIGHT_BRACE
    : '}'
    ;

LEFT_PARENTHESIS
    : '('
    ;

RIGHT_PARENTHESIS
    : ')'
    ;

UNDERSCORE
    : '_'
    ;

MUL
    : '*'
    ;

DIV
    : '/'
    ;

MOD
    : '%'
    ;

PLUS
    : '+'
    ;

MINUS
    : '-'
    ;

GT
    : '>'
    ;

LT
    : '<'
    ;

GE
    : '>='
    ;

LE
    : '<='
    ;

EQ
    : '=='
    ;

NEQ
    : '!='
    ;

AND
    : '&&'
    ;

OR
    : '||'
    ;

LINE_COMMENT
    : '//' ~[\r\n]* -> skip
    ;

WS
    : [ \t\r\n]+ -> skip
    ;

UnknownToken
    : .
    ;


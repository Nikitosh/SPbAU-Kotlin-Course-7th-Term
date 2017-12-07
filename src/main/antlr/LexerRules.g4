lexer grammar LexerRules;

@lexer::header {
    import ru.spbau.mit.exceptions.InterpretationException;
}

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

Identifier
    : (Letter | UNDERSCORE) (Letter | Digit | UNDERSCORE)*
    ;

fragment
Letter
    : 'a'..'z'
    | 'A'..'Z'
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
    : MINUS? '0' [0-9]+ { if (true) { throw new InterpretationException(getLine(), "Number with leading zeros found"); } }
    ;

InvalidIdentifier
    : [0-9]([0-9a-zA-Z])+ { if (true) { throw new InterpretationException(getLine(), "Invalid identifier found"); } }
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
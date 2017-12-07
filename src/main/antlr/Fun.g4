grammar Fun;
import LexerRules;

@parser::header {
    import ru.spbau.mit.exceptions.InterpretationException;
}

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
    : functionCall                                   # functionCallExpression
    | identifier                                     # identifierExpression
    | literal                                        # literalExpression
    | LEFT_PARENTHESIS expression RIGHT_PARENTHESIS  # parenthesisExpression
    | expression op = (MUL | DIV | MOD) expression   # binaryExpression
    | expression op = (PLUS | MINUS) expression      # binaryExpression
    | expression op = (GT | LT | GE | LE) expression # binaryExpression
    | expression op = (EQ | NEQ) expression          # binaryExpression
    | expression op = AND expression                 # logicalExpression
    | expression op = OR expression                  # logicalExpression
    ;

functionCall
    : identifier LEFT_PARENTHESIS arguments RIGHT_PARENTHESIS
    ;

arguments
    : (expression (COMMA expression)*)?
    ;

identifier
    : Identifier
    ;

literal
    : Number
    ;
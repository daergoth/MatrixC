grammar Matrix;

program
    : symbolDeclaration+ EOF
    ;

symbolDeclaration
    : variableDeclaration SEMI
    | functionDecl
    ;

variableDeclaration
    : type id '=' expr
    ;

functionDecl
    : 'function' returnType id LPAREN functionDeclParameterList? RPAREN block
    ;

returnType
    : type
    | 'void'
    ;

functionDeclParameterList
    : functionDeclParameter (COMMA functionDeclParameter)*
    ;

functionDeclParameter
    : type id
    ;

block
    : LBRACE statement+ RBRACE
    ;

statement
    : simpleStatement SEMI                                                      # simpleStatementStatement
    | controllBlock                                                             # controllBlockStatement
    ;

simpleStatement
    : variableDeclaration                                                               # declareStatement
    | id '=' expr                                                               # assignStatement
    | functionCall                                                              # functionCallStatement
    | 'return' expr                                                             # returnStatement
    ;

controllBlock
    : 'if' parenLogicExpr block                                                                     # ifStatement
    | 'if' parenLogicExpr ifBlock=block 'else' elseBlock=block                                      # ifElseStatement
    | 'while' parenLogicExpr block                                                                  # whileStatement
    | 'for' LPAREN decl=variableDeclaration SEMI logic=logicExpr SEMI update=simpleStatement RPAREN block   # forStatement
    | block                                                                                         # blockStatement
    ;

parenLogicExpr
    : LPAREN logicExpr RPAREN
    ;

logicExpr
    : expr                                                                      # exprLogicExpression
    | leftExpr=expr relation rightExpr=expr                                     # relationLogicExpression
    ;

expr
    : term                                                                      # termExpression
    | functionCall                                                              # functionCallExpression
    | leftOperand=expr bin_operator rightOperand=expr                           # binOperatorExpression
    | LPAREN expr RPAREN                                                        # parenthesisExpression
    ;

functionCall
    : id LPAREN functionCallParameterList? RPAREN
    ;

functionCallParameterList
    : expr (COMMA expr)*
    ;

term
    : id                                                                        # idTerm
    | rational                                                                  # rationalTerm
    | matrix                                                                    # matrixTerm
    ;


bin_operator
    : '+'
    | '-'
    | '/'
    | '*'
    | '^'
    | '#'
    ;

type
    : rationalType
    | matrixType
    ;

rationalType
    : 'rational'
    ;

matrixType
    : 'matrix'
    ;

rational
    : INTEGER '|' INTEGER
    ;

matrix
    : LBRACE matrix_row (COMMA matrix_row)* RBRACE
    ;

matrix_row
    : LBRACE matrix_element (COMMA matrix_element)* RBRACE
    ;

matrix_element
    : rational
    | id
    ;

relation
    : '<'
    | '>'
    | '<='
    | '>='
    | '=='
    | '!='
    ;

id
    : ID
    ;

INTEGER
    : '-'? [0-9]+
    ;

ID
    : [a-z][a-zA-Z0-9_]*
    ;

LPAREN
    : '('
    ;

RPAREN
    : ')'
    ;

LBRACE
    : '{'
    ;

RBRACE
    : '}'
    ;

COMMA
    : ','
    ;

SEMI
    : ';'
    ;

WS
   : [ \r\n\t] -> skip
   ;
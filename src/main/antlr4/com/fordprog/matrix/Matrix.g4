grammar Matrix;

program
    : (declaration SEMI | functionDecl)+ EOF
    ;

declaration
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
    : functionDeclParameter functionDeclParameter*
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
    : declaration                                                               # declareStatement
    | id '=' expr                                                               # assignStatement
    | functionCall                                                              # functionCallStatement
    | 'return' expr                                                             # returnStatement
    ;

controllBlock
    : 'if' parenLogicExpr block                                                                     # ifStatement
    | 'if' parenLogicExpr ifBlock=block 'else' elseBlock=block                                      # ifElseStatement
    | 'while' parenLogicExpr block                                                                  # whileStatement
    | 'for' LPAREN decl=declaration SEMI logic=logicExpr SEMI update=simpleStatement RPAREN block   # forStatement
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
    : rationalPart '|' rationalPart
    ;

rationalPart
    : INTEGER
    | id
    ;

matrix
    : LBRACE matrix_row (COMMA matrix_row)* RBRACE
    ;

matrix_row
    : LBRACE INTEGER (COMMA INTEGER)* RBRACE
    ;

relation
    : compareSigns
    | equalSigns
    ;

compareSigns
    : '<'
    | '>'
    | '<='
    | '>='
    ;

equalSigns
    : '=='
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
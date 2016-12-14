package com.fordprog.matrix.interpreter.error.semantic;


import com.fordprog.matrix.interpreter.CodePoint;

public class IdentifierAlreadyDeclaredSemanticError extends SemanticError {

  private final String identifier;

  private final CodePoint originalDeclaration;

  public IdentifierAlreadyDeclaredSemanticError(CodePoint codePoint, String identifier,
                                                CodePoint originalDeclaration) {
    super(codePoint);
    this.identifier = identifier;
    this.originalDeclaration = originalDeclaration;
  }

  @Override
  public String getMessage() {
    return "Symbol with identifier \'" + identifier + "\' already declared at "
        + originalDeclaration + ". Duplicate at " + getCodePoint();
  }
}

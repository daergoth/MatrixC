package com.fordprog.matrix.interpreter.error.semantic;


import com.fordprog.matrix.interpreter.CodePoint;

public class UndeclaredIdentifierSemanticError extends SemanticError {

  private final String identifier;

  public UndeclaredIdentifierSemanticError(CodePoint codePoint, String identifier) {
    super(codePoint);

    this.identifier = identifier;
  }

  @Override
  public String getMessage() {
    return "Identifier \'" + identifier + "\' is not declared at " + getCodePoint();
  }

  public String getIdentifier() {
    return identifier;
  }
}

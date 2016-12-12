package com.fordprog.matrix.interpreter.error;

import com.fordprog.matrix.interpreter.CodePoint;


public class OperationWithVoidFunctionSemanticError extends SemanticError {

  private final String identifier;

  public OperationWithVoidFunctionSemanticError(CodePoint codePoint, String identifier) {
    super(codePoint);

    this.identifier = identifier;
  }

  @Override
  public String getMessage() {
    return "Void function \'" + identifier + "\' can not be used in operation at " + getCodePoint();
  }

  public String getIdentifier() {
    return identifier;
  }
}

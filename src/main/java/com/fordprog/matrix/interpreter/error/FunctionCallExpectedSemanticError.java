package com.fordprog.matrix.interpreter.error;


import com.fordprog.matrix.interpreter.CodePoint;

public class FunctionCallExpectedSemanticError extends SemanticError {

  private final String identifier;

  public FunctionCallExpectedSemanticError(CodePoint codePoint, String identifier) {
    super(codePoint);

    this.identifier = identifier;
  }

  @Override
  public String getMessage() {
    return "Expected function call of \'" + identifier + "\' at " + getCodePoint()
        + ". Maybe you are missing the parentheses?null";
  }

  public String getIdentifier() {
    return identifier;
  }
}

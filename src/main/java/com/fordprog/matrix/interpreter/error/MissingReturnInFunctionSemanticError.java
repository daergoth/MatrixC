package com.fordprog.matrix.interpreter.error;

import com.fordprog.matrix.interpreter.CodePoint;

public class MissingReturnInFunctionSemanticError extends SemanticError {

  private final String identifier;

  public MissingReturnInFunctionSemanticError(CodePoint codePoint, String identifier) {
    super(codePoint);

    this.identifier = identifier;
  }

  @Override
  public String getMessage() {
    return "Missing return statement in function \'" + identifier + "\' declared at "
        + getCodePoint();
  }

  public String getIdentifier() {
    return identifier;
  }
}

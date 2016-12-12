package com.fordprog.matrix.interpreter.error;

import com.fordprog.matrix.interpreter.CodePoint;

public class VoidParameterInFunctionCallSemanticError extends SemanticError {

  private final String identifier;

  private final int parameterNumber;

  public VoidParameterInFunctionCallSemanticError(
      CodePoint codePoint, String identifier, int parameterNumber) {
    super(codePoint);

    this.identifier = identifier;
    this.parameterNumber = parameterNumber;
  }

  @Override
  public String getMessage() {
    return "Void parameter in parameter number " + parameterNumber + " when calling function \'"
        + identifier + "\' at " + getCodePoint();
  }

  public String getIdentifier() {
    return identifier;
  }

  public int getParameterNumber() {
    return parameterNumber;
  }
}

package com.fordprog.matrix.interpreter.error;


import com.fordprog.matrix.interpreter.CodePoint;
import com.fordprog.matrix.interpreter.type.Type;

public class ReturnTypeMismatchSemanticError extends TypeMismatchSemanticError {

  private final String identifier;

  public ReturnTypeMismatchSemanticError(CodePoint codePoint,
                                         Type expected,
                                         Type actual, String identifier) {
    super(codePoint, expected, actual);

    this.identifier = identifier;
  }

  @Override
  public String getMessage() {
    return "Type mismatch when returning from non-void function \'" + identifier + "\' at "
        + getCodePoint() + ". Expected \'" + getExpected() + "\', but got \'" + getActual() + "\'";
  }
}

package com.fordprog.matrix.interpreter.error;


import com.fordprog.matrix.interpreter.CodePoint;
import com.fordprog.matrix.interpreter.type.Type;

public class TypeMismatchSemanticError extends SemanticError {

  private final Type expected;

  private final Type actual;

  public TypeMismatchSemanticError(CodePoint codePoint, Type expected, Type actual) {
    super(codePoint);

    this.expected = expected;
    this.actual = actual;
  }

  @Override
  public String getMessage() {
    return "Type mismatch at " + getCodePoint() + ". Expected: \'" + expected + "\', but got \'"
        + actual + "\'";
  }

  public Type getExpected() {
    return expected;
  }

  public Type getActual() {
    return actual;
  }
}

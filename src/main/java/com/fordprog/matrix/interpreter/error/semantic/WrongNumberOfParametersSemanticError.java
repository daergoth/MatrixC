package com.fordprog.matrix.interpreter.error.semantic;

import com.fordprog.matrix.interpreter.CodePoint;


public class WrongNumberOfParametersSemanticError extends SemanticError {

  private final int expected;

  private final int actual;

  public WrongNumberOfParametersSemanticError(CodePoint codePoint, int expected, int actual) {
    super(codePoint);

    this.expected = expected;
    this.actual = actual;
  }

  @Override
  public String getMessage() {
    return "Wrong number of parameters at " + getCodePoint() + ", Expected " + expected
        + ", but got " + actual;
  }

  public int getExpected() {
    return expected;
  }

  public int getActual() {
    return actual;
  }
}

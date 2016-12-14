package com.fordprog.matrix.interpreter.error.semantic;


import com.fordprog.matrix.interpreter.CodePoint;

public abstract class SemanticError {

  private final CodePoint codePoint;

  public SemanticError(CodePoint codePoint) {
    this.codePoint = codePoint;
  }

  public abstract String getMessage();

  public CodePoint getCodePoint() {
    return codePoint;
  }

  @Override
  public String toString() {
    return getMessage();
  }
}

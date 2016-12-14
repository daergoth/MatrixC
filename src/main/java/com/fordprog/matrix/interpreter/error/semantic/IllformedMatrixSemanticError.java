package com.fordprog.matrix.interpreter.error.semantic;

import com.fordprog.matrix.interpreter.CodePoint;


public class IllformedMatrixSemanticError extends SemanticError {

  public IllformedMatrixSemanticError(CodePoint codePoint) {
    super(codePoint);
  }

  @Override
  public String getMessage() {
    return "Illformed matrix literal at " + getCodePoint();
  }
}

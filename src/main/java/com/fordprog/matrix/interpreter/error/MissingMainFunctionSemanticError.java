package com.fordprog.matrix.interpreter.error;

import com.fordprog.matrix.interpreter.CodePoint;

public class MissingMainFunctionSemanticError extends SemanticError {

  public MissingMainFunctionSemanticError(CodePoint codePoint) {
    super(codePoint);
  }

  @Override
  public String getMessage() {
    return "Function with name \'main\' is missing!";
  }
}

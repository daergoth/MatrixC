package com.fordprog.matrix.interpreter.error;

import com.fordprog.matrix.interpreter.CodePoint;

public class ReturnInVoidFunctionSemanticError extends SemanticError {

  public ReturnInVoidFunctionSemanticError(CodePoint codePoint) {
    super(codePoint);
  }

  @Override
  public String getMessage() {
    return "Return statement in void function at " + getCodePoint();
  }
}

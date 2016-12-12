package com.fordprog.matrix.interpreter.error;


import com.fordprog.matrix.interpreter.CodePoint;

public class InvalidMainFunctionSignatureSemanticError extends SemanticError {

  public InvalidMainFunctionSignatureSemanticError(
      CodePoint codePoint) {
    super(codePoint);
  }

  @Override
  public String getMessage() {
    return "Function named \'main\' must have \'void\' return type and empty parameter list!";
  }
}

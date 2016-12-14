package com.fordprog.matrix.interpreter.error.semantic;


import com.fordprog.matrix.interpreter.CodePoint;

public class AssignmentToFunctionSemanticError extends SemanticError {

  public AssignmentToFunctionSemanticError(CodePoint codePoint) {
    super(codePoint);
  }

  @Override
  public String getMessage() {
    return "Assignment to function is not possible at " + getCodePoint();
  }

}

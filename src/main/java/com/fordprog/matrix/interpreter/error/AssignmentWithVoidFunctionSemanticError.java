package com.fordprog.matrix.interpreter.error;

import com.fordprog.matrix.interpreter.CodePoint;


public class AssignmentWithVoidFunctionSemanticError extends SemanticError {

  private final String identifier;

  private final CodePoint declarationPoint;

  public AssignmentWithVoidFunctionSemanticError(
      CodePoint codePoint, String identifier, CodePoint declarationPoint) {
    super(codePoint);
    this.identifier = identifier;
    this.declarationPoint = declarationPoint;
  }

  @Override
  public String getMessage() {
    return "Assignment using void function \'" + identifier + "\' declared at " + declarationPoint
        + ", is not possible at " + getCodePoint();
  }
}

package com.fordprog.matrix.interpreter.error.semantic;


import com.fordprog.matrix.interpreter.CodePoint;
import com.fordprog.matrix.interpreter.type.Type;

public class ParameterTypeMismatchSemanticError extends TypeMismatchSemanticError {

  private final String identifier;

  private final int parameterNum;

  public ParameterTypeMismatchSemanticError(CodePoint codePoint,
                                            Type expected,
                                            Type actual, String identifier, int parameterNum) {
    super(codePoint, expected, actual);

    this.identifier = identifier;
    this.parameterNum = parameterNum;
  }

  @Override
  public String getMessage() {
    return "Parameter type mismatch in parameter number " + parameterNum
        + " when calling function \'" + identifier + "\' at " + getCodePoint() + ". Expected "
        + getExpected() + ", but got " + getActual();
  }

  public String getIdentifier() {
    return identifier;
  }

  public int getParameterNum() {
    return parameterNum;
  }
}

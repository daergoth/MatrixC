package com.fordprog.matrix.interpreter.type;


import com.fordprog.matrix.interpreter.scope.Symbol;

import java.util.List;

public class Function {

  private final Type returnType;

  private final List<Symbol> parameterList;

  public Function(Type returnType, List<Symbol> parameterList) {
    this.returnType = returnType;
    this.parameterList = parameterList;
  }

  public Type getReturnType() {
    return returnType;
  }

  public List<Symbol> getParameterList() {
    return parameterList;
  }
}

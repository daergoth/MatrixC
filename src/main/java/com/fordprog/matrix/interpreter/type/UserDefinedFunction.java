package com.fordprog.matrix.interpreter.type;


import com.fordprog.matrix.interpreter.scope.Symbol;

import java.util.List;

public class UserDefinedFunction extends Function {

  public UserDefinedFunction(Type returnType,
                             List<Symbol> parameterList) {
    super(returnType, parameterList);
  }

}

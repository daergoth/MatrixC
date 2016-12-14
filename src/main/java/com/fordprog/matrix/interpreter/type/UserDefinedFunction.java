package com.fordprog.matrix.interpreter.type;


import com.fordprog.matrix.MatrixParser;
import com.fordprog.matrix.interpreter.execution.FunctionVisitor;
import com.fordprog.matrix.interpreter.semantic.Symbol;

import java.util.List;

public class UserDefinedFunction extends Function {

  private final MatrixParser.FunctionDeclContext functionDeclContext;

  public UserDefinedFunction(Type returnType,
                             List<Symbol> parameterList,
                             MatrixParser.FunctionDeclContext functionDeclContext) {
    super(returnType, parameterList);

    this.functionDeclContext = functionDeclContext;
  }

  @Override
  public void invoke(FunctionVisitor functionVisitor) {
    functionVisitor.visit(this);
  }

  public MatrixParser.FunctionDeclContext getFunctionDeclContext() {
    return functionDeclContext;
  }


}

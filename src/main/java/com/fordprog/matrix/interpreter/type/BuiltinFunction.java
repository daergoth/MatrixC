package com.fordprog.matrix.interpreter.type;


import com.fordprog.matrix.interpreter.execution.FunctionVisitor;
import com.fordprog.matrix.interpreter.semantic.Symbol;

import java.util.List;
import java.util.stream.Collectors;

public class BuiltinFunction extends Function {

  private final java.util.function.Function<List<Object>, Object> implementation;

  public BuiltinFunction(Type returnType,
                         List<Symbol> parameterList,
                         java.util.function.Function<List<Object>, Object> implementation) {
    super(returnType, parameterList);

    this.implementation = implementation;
  }

  @Override
  public void invoke(FunctionVisitor functionVisitor) {
    functionVisitor.visit(this);
  }

  public void call(Symbol targetSymbol) {
    List<Object> passedArgs = getParameterList().stream()
        .map(symbol -> symbol.getValue()).collect(Collectors.toList());

    targetSymbol.setValue(implementation.apply(passedArgs), getReturnType());
  }
}

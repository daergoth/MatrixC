package com.fordprog.matrix.interpreter.execution.stdlib;

import com.fordprog.matrix.interpreter.CodePoint;
import com.fordprog.matrix.interpreter.semantic.Symbol;
import com.fordprog.matrix.interpreter.type.BuiltinFunction;
import com.fordprog.matrix.interpreter.type.Type;

import java.util.List;


public abstract class BuiltinDeclarationSource {

  public static final Object VOID_RETURN = null;

  public abstract List<Symbol> getDeclarations();

  protected Symbol createBuiltinParameterSymbol(String identifier, Type type) {
    return Symbol.builder()
        .identifier(identifier)
        .type(type)
        .firstOccurrence(new CodePoint(0, 0))
        .build();
  }

  protected Symbol createBuiltinFunctionSymbol(String identifier, BuiltinFunction function) {
    return Symbol.builder()
        .identifier(identifier)
        .type(Type.FUNCTION)
        .firstOccurrence(new CodePoint(0, 0))
        .value(function)
        .build();
  }

}

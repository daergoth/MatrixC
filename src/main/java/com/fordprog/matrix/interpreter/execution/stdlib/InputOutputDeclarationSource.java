package com.fordprog.matrix.interpreter.execution.stdlib;

import com.fordprog.matrix.interpreter.scope.Symbol;
import com.fordprog.matrix.interpreter.type.BuiltinFunction;
import com.fordprog.matrix.interpreter.type.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class InputOutputDeclarationSource extends BuiltinDeclarationSource {

  @Override
  public List<Symbol> getDeclarations() {
    List<Symbol> declaredSymbols = new ArrayList<>();

    BuiltinFunction printMatrixBuiltinFunction =
        new BuiltinFunction(Type.VOID,
            Collections.singletonList(createBuiltinParameterSymbol("m", Type.MATRIX)),
            this::print);

    BuiltinFunction printRationalBuiltinFunction =
        new BuiltinFunction(Type.VOID,
            Collections.singletonList(createBuiltinParameterSymbol("r", Type.RATIONAL)),
            this::print);

    declaredSymbols.add(createBuiltinFunctionSymbol("print_matrix", printMatrixBuiltinFunction));

    declaredSymbols
        .add(createBuiltinFunctionSymbol("print_rational", printRationalBuiltinFunction));

    return declaredSymbols;
  }

  private Object print(List<Object> parameters) {
    System.out.println(parameters.get(0).toString());

    return VOID_RETURN;
  }

}

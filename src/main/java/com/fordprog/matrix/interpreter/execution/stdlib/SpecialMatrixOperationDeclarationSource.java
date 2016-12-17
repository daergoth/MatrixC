package com.fordprog.matrix.interpreter.execution.stdlib;

import com.fordprog.matrix.interpreter.semantic.Symbol;
import com.fordprog.matrix.interpreter.type.BuiltinFunction;
import com.fordprog.matrix.interpreter.type.Matrix;
import com.fordprog.matrix.interpreter.type.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SpecialMatrixOperationDeclarationSource extends BuiltinDeclarationSource {

  private final MatrixOperation matrixOperation;

  public SpecialMatrixOperationDeclarationSource() {
    matrixOperation = MatrixOperation.getInstance();
  }

  @Override
  public List<Symbol> getDeclarations() {
    List<Symbol> declaredSymbols = new ArrayList<>();

    BuiltinFunction inverseBuiltinFunction =
        new BuiltinFunction(Type.MATRIX,
            Collections.singletonList(createBuiltinParameterSymbol("m", Type.MATRIX)),
            this::inverseMatrix
        );

    BuiltinFunction determinantBuiltinFunction =
        new BuiltinFunction(Type.RATIONAL,
            Collections.singletonList(createBuiltinParameterSymbol("m", Type.MATRIX)),
            this::determinantOfMatrix
        );

    BuiltinFunction transposeBuiltinFunction =
        new BuiltinFunction(Type.MATRIX,
            Collections.singletonList(createBuiltinParameterSymbol("m", Type.MATRIX)),
            this::transposeMatrix
        );

    declaredSymbols.add(createBuiltinFunctionSymbol("inverse", inverseBuiltinFunction));

    declaredSymbols.add(createBuiltinFunctionSymbol("determinant", determinantBuiltinFunction));

    declaredSymbols.add(createBuiltinFunctionSymbol("transpose", transposeBuiltinFunction));

    return declaredSymbols;
  }

  private Object inverseMatrix(List<Object> parameters) {
    return matrixOperation.inverse((Matrix) parameters.get(0));
  }

  private Object determinantOfMatrix(List<Object> parameters) {
    return matrixOperation.determinant((Matrix) parameters.get(0));
  }

  private Object transposeMatrix(List<Object> parameters) {
    return matrixOperation.determinant((Matrix) parameters.get(0));
  }

  //TODO: gauss, equationsystem, eigenvalue, eigenvector

}

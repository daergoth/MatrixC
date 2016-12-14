package com.fordprog.matrix.interpreter.execution.stdlib;

import com.fordprog.matrix.MatrixLexer;
import com.fordprog.matrix.MatrixParser;
import com.fordprog.matrix.interpreter.error.runtime.InvalidReadRuntimeError;
import com.fordprog.matrix.interpreter.semantic.Scope;
import com.fordprog.matrix.interpreter.semantic.Symbol;
import com.fordprog.matrix.interpreter.type.BuiltinFunction;
import com.fordprog.matrix.interpreter.type.Matrix;
import com.fordprog.matrix.interpreter.type.Rational;
import com.fordprog.matrix.interpreter.type.Type;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;


public class InputOutputDeclarationSource extends BuiltinDeclarationSource {

  private final Scanner in = new Scanner(System.in);

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

    BuiltinFunction readMatrixBuiltinFunction =
        new BuiltinFunction(Type.MATRIX,
            Collections.emptyList(),
            this::readMatrix);

    BuiltinFunction readRationalBuiltinFunction =
        new BuiltinFunction(Type.RATIONAL,
            Collections.emptyList(),
            this::readRational);

    declaredSymbols.add(createBuiltinFunctionSymbol("print_matrix", printMatrixBuiltinFunction));

    declaredSymbols
        .add(createBuiltinFunctionSymbol("print_rational", printRationalBuiltinFunction));

    declaredSymbols.add(createBuiltinFunctionSymbol("read_matrix", readMatrixBuiltinFunction));

    declaredSymbols.add(createBuiltinFunctionSymbol("read_rational", readRationalBuiltinFunction));

    return declaredSymbols;
  }

  private Object print(List<Object> parameters) {
    System.out.println(parameters.get(0).toString());

    return VOID_RETURN;
  }

  private Object readMatrix(List<Object> parameters) {
    String rationalString = in.nextLine();

    MatrixLexer matrixLexer = new MatrixLexer(new ANTLRInputStream(rationalString));

    MatrixParser matrixParser = new MatrixParser(new CommonTokenStream(matrixLexer));

    matrixParser.setErrorHandler(new BailErrorStrategy());

    try {
      MatrixParser.MatrixContext matrixContext = matrixParser.matrix();

      return Matrix.fromMatrixContext(matrixContext, Scope.NULL_SCOPE);
    } catch (ParseCancellationException e) {
      throw new InvalidReadRuntimeError("Invalid input read from stdin! Expected matrix format!");
    }
  }

  private Object readRational(List<Object> parameters) {
    String rationalString = in.nextLine();

    MatrixLexer matrixLexer = new MatrixLexer(new ANTLRInputStream(rationalString));

    MatrixParser matrixParser = new MatrixParser(new CommonTokenStream(matrixLexer));

    try {
      MatrixParser.RationalContext rationalContext = matrixParser.rational();

      return Rational.fromRationalContext(rationalContext);
    } catch (ParseCancellationException e) {
      throw new InvalidReadRuntimeError("Invalid input read from stdin! Expected rational format!");
    }
  }

}

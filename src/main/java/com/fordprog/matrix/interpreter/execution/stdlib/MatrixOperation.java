package com.fordprog.matrix.interpreter.execution.stdlib;


import com.fordprog.matrix.interpreter.error.runtime.InvalidOperationParameterRuntimeError;
import com.fordprog.matrix.interpreter.type.Matrix;
import com.fordprog.matrix.interpreter.type.Rational;

import java.math.BigInteger;

public class MatrixOperation {

  private static MatrixOperation instance = new MatrixOperation();

  private final RationalOperation rationalOperation;

  private MatrixOperation() {
    rationalOperation = RationalOperation.getInstance();
  }

  public static MatrixOperation getInstance() {
    return instance;
  }

  public Matrix add(Matrix a, Matrix b) {
    if (isMatchingMatrices(a, b)) {
      Rational value[][] = new Rational[a.getRowNum()][a.getColumnNum()];

      for (int r = 0; r < a.getRowNum(); ++r) {
        for (int c = 0; c < a.getColumnNum(); ++c) {
          value[r][c] =
              rationalOperation.add(a.getValueAtPosition(r, c), b.getValueAtPosition(r, c));
        }
      }

      return new Matrix(value);
    } else {
      throw new InvalidOperationParameterRuntimeError("Parameter matrices don't match dimensions!");
    }
  }

  public Matrix subtract(Matrix a, Matrix b) {
    if (isMatchingMatrices(a, b)) {
      Rational value[][] = new Rational[a.getRowNum()][a.getColumnNum()];

      for (int r = 0; r < a.getRowNum(); ++r) {
        for (int c = 0; c < a.getColumnNum(); ++c) {
          value[r][c] =
              rationalOperation.subtract(a.getValueAtPosition(r, c), b.getValueAtPosition(r, c));
        }
      }

      return new Matrix(value);
    } else {
      throw new InvalidOperationParameterRuntimeError("Parameter matrices don't match dimensions!");
    }
  }

  public Matrix multiply(Matrix a, Matrix b) {
    if (a.getColumnNum() == b.getRowNum()) {
      Rational sum;
      Rational value[][] = new Rational[a.getRowNum()][b.getColumnNum()];

      for (int r = 0; r < a.getRowNum(); ++r) {
        for (int c = 0; c < b.getColumnNum(); ++c) {
          sum = new Rational(BigInteger.ZERO, BigInteger.ONE);

          for (int i = 0; i < b.getRowNum(); ++i) {
            sum = rationalOperation.add(sum, rationalOperation
                .multiply(a.getValueAtPosition(r, i), b.getValueAtPosition(i, c)));
          }

          value[r][c] = sum;
        }
      }

      return new Matrix(value);
    } else {
      throw new InvalidOperationParameterRuntimeError("Parameter matrices cannot be multiplied!");
    }
  }

  //TODO
  public Matrix inverse(Matrix a) {
    return null;
  }

  //TODO
  public Rational determinant(Matrix a) {
    return null;
  }

  public Matrix transpose(Matrix a) {
    Rational value[][] = new Rational[a.getColumnNum()][a.getRowNum()];

    for (int r = 0; r < a.getRowNum(); ++r) {
      for (int c = 0; c < a.getColumnNum(); ++c) {
        value[c][r] = new Rational(a.getValueAtPosition(r, c));
      }
    }

    return new Matrix(value);
  }

  public Matrix scalarMultiply(Matrix a, Rational s) {
    Rational value[][] = new Rational[a.getRowNum()][a.getColumnNum()];

    for (int r = 0; r < a.getRowNum(); ++r) {
      for (int c = 0; c < a.getColumnNum(); ++c) {
        value[r][c] = rationalOperation.multiply(a.getValueAtPosition(r, c), s);
      }
    }

    return new Matrix(value);
  }

  //TODO gauss, eigens, equationsystem

  private boolean isMatchingMatrices(Matrix a, Matrix b) {
    return a.getRowNum() == b.getRowNum() && a.getColumnNum() == b.getColumnNum();
  }
}

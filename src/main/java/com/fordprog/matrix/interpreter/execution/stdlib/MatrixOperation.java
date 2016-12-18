package com.fordprog.matrix.interpreter.execution.stdlib;


import com.fordprog.matrix.interpreter.error.runtime.InvalidOperationParameterRuntimeError;
import com.fordprog.matrix.interpreter.type.Matrix;
import com.fordprog.matrix.interpreter.type.Rational;

import java.math.BigInteger;

public class MatrixOperation {

  private static MatrixOperation instance = new MatrixOperation();

  private final Rational epsilon;

  private final RationalOperation rationalOperation;

  private final LogicOperation logicOperation;

  private MatrixOperation() {
    rationalOperation = RationalOperation.getInstance();
    logicOperation = LogicOperation.getInstance();

    // 1e-10
    epsilon = new Rational(BigInteger.ONE, BigInteger.valueOf(10000000000L));
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

  public Matrix gaussElimination(Matrix a, Matrix v) {

    if (v.getRowNum() != 1) {
      throw new InvalidOperationParameterRuntimeError(
          "Second parameter of Gauss elimination must be a row vector!");
    }

    if (a.getRowNum() != v.getColumnNum()) {
      throw new InvalidOperationParameterRuntimeError(
          "Gauss elimination parameters' row number don't match!");
    }

    Rational aValue[][] = new Rational[a.getRowNum()][a.getColumnNum()];
    for (int i = 0; i < a.getRowNum(); i++) {
      aValue[i] = a.getValue()[i].clone();
    }

    Rational bValue[] = v.getValue()[0].clone();

    int N = v.getColumnNum();

    for (int p = 0; p < N; p++) {

      // find pivot row and swap
      int max = p;
      for (int i = p + 1; i < N; i++) {
        if (logicOperation
            .greaterThan(rationalOperation.abs(aValue[i][p]), rationalOperation.abs(aValue[max][p]))
            .equals(Rational.TRUE)) {
          max = i;
        }
      }

      Rational[] temp = aValue[p];
      aValue[p] = aValue[max];
      aValue[max] = temp;

      Rational t = bValue[p];
      bValue[p] = bValue[max];
      bValue[max] = t;

      // singular or nearly singular
      if (logicOperation.lessThanOrEqual(rationalOperation.abs(aValue[p][p]), epsilon)
          .equals(Rational.TRUE)) {
        throw new InvalidOperationParameterRuntimeError("Matrix is singular or nearly singular");
      }

      // pivot within A and b
      for (int i = p + 1; i < N; i++) {
        Rational alpha = rationalOperation.divide(aValue[i][p], aValue[p][p]);
        bValue[i] =
            rationalOperation.subtract(bValue[i], rationalOperation.multiply(alpha, bValue[p]));
        for (int j = p; j < N; j++) {
          aValue[i][j] =
              rationalOperation
                  .subtract(aValue[i][j], rationalOperation.multiply(alpha, aValue[p][j]));
        }
      }
    }

    Rational result[][] = new Rational[a.getRowNum()][a.getColumnNum() + 1];

    for (int r = 0; r < a.getRowNum(); ++r) {
      for (int c = 0; c < a.getColumnNum(); ++c) {
        result[r][c] = aValue[r][c];
      }
    }

    for (int r = 0; r < bValue.length; ++r) {
      result[r][a.getColumnNum()] = bValue[r];
    }

    return new Matrix(result);
  }

  public Matrix solveLinearSystem(Matrix a, Matrix v) {
    if (v.getRowNum() != 1) {
      throw new InvalidOperationParameterRuntimeError(
          "Second parameter of linear equation solving must be a row vector!");
    }

    if (a.getRowNum() != v.getColumnNum()) {
      throw new InvalidOperationParameterRuntimeError(
          "Linear equation system solving parameters' row number don't match!");
    }

    Rational aValue[][] = new Rational[a.getRowNum()][a.getColumnNum()];
    for (int i = 0; i < a.getRowNum(); i++) {
      aValue[i] = a.getValue()[i].clone();
    }

    Rational bValue[] = v.getValue()[0].clone();

    int N = v.getColumnNum();

    for (int p = 0; p < N; p++) {

      // find pivot row and swap
      int max = p;
      for (int i = p + 1; i < N; i++) {
        if (logicOperation
            .greaterThan(rationalOperation.abs(aValue[i][p]), rationalOperation.abs(aValue[max][p]))
            .equals(Rational.TRUE)) {
          max = i;
        }
      }

      Rational[] temp = aValue[p];
      aValue[p] = aValue[max];
      aValue[max] = temp;

      Rational t = bValue[p];
      bValue[p] = bValue[max];
      bValue[max] = t;

      // singular or nearly singular
      if (logicOperation.lessThanOrEqual(rationalOperation.abs(aValue[p][p]), epsilon)
          .equals(Rational.TRUE)) {
        throw new InvalidOperationParameterRuntimeError("Matrix is singular or nearly singular");
      }

      // pivot within A and b
      for (int i = p + 1; i < N; i++) {
        Rational alpha = rationalOperation.divide(aValue[i][p], aValue[p][p]);
        bValue[i] =
            rationalOperation.subtract(bValue[i], rationalOperation.multiply(alpha, bValue[p]));
        for (int j = p; j < N; j++) {
          aValue[i][j] =
              rationalOperation
                  .subtract(aValue[i][j], rationalOperation.multiply(alpha, aValue[p][j]));
        }
      }
    }

    // back substitution
    Rational[][] x = new Rational[1][N];
    for (int i = N - 1; i >= 0; i--) {
      Rational sum = new Rational(BigInteger.ZERO, BigInteger.ONE);
      for (int j = i + 1; j < N; j++) {
        sum = rationalOperation.add(sum, rationalOperation.multiply(aValue[i][j], x[0][j]));
      }
      x[0][i] = rationalOperation.divide(rationalOperation.subtract(bValue[i], sum), aValue[i][i]);
    }

    return new Matrix(x);
  }

  //TODO eigens

  private boolean isMatchingMatrices(Matrix a, Matrix b) {
    return a.getRowNum() == b.getRowNum() && a.getColumnNum() == b.getColumnNum();
  }
}

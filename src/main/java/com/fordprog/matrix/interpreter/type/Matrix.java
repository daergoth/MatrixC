package com.fordprog.matrix.interpreter.type;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;


public class Matrix {

  private final RealMatrix valueMatrix;

  public Matrix(int rowNum, int columnNum) {
    valueMatrix = new Array2DRowRealMatrix(rowNum, columnNum);
  }

  public Matrix(Rational rational) {
    valueMatrix = new Array2DRowRealMatrix(1, 1);
    valueMatrix.setEntry(0, 0, rational.getAsDouble());
  }

  public Matrix(Matrix matrix) {
    valueMatrix = new Array2DRowRealMatrix(matrix.getValue());
  }

  public Matrix(Rational[][] rationalArray) {
    int rowNum = rationalArray.length;
    int columnNum = rationalArray[0].length;

    valueMatrix = new Array2DRowRealMatrix(rowNum, columnNum);

    for (int r = 0; r < rowNum; ++r) {
      for (int c = 0; c < columnNum; ++c) {
        valueMatrix.setEntry(r, c, rationalArray[r][c].getAsDouble());
      }
    }
  }

  public static Matrix fromRational(Rational rational) {
    return new Matrix(rational);
  }

  public boolean canBeConverted() {
    return valueMatrix.getColumnDimension() == 1 && valueMatrix.getRowDimension() == 1;
  }

  public double[][] getValue() {
    return valueMatrix.getData();
  }
}

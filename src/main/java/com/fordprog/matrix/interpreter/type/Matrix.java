package com.fordprog.matrix.interpreter.type;

import com.fordprog.matrix.MatrixParser;
import com.fordprog.matrix.interpreter.error.runtime.InvalidReadRuntimeError;
import com.fordprog.matrix.interpreter.semantic.Scope;


public class Matrix {

  private final int rowNum;

  private final int columnNum;

  private final Rational[][] valueMatrix;

  public Matrix(int rowNum, int columnNum) {
    this.rowNum = rowNum;
    this.columnNum = columnNum;

    valueMatrix = new Rational[rowNum][columnNum];
  }

  public Matrix(Rational rational) {
    this.rowNum = 1;
    this.columnNum = 1;

    valueMatrix = new Rational[1][1];
    valueMatrix[0][0] = rational;
  }

  public Matrix(Matrix matrix) {
    this.rowNum = matrix.getRowNum();
    this.columnNum = matrix.getColumnNum();

    valueMatrix = matrix.getValue();
  }

  public Matrix(Rational[][] rationalArray) {
    int rowNum = rationalArray.length;
    int columnNum = rationalArray[0].length;

    this.rowNum = rowNum;
    this.columnNum = columnNum;

    valueMatrix = new Rational[rowNum][columnNum];

    for (int r = 0; r < rowNum; ++r) {
      for (int c = 0; c < columnNum; ++c) {
        valueMatrix[r][c] = new Rational(rationalArray[r][c]);
      }
    }
  }

  public static Matrix fromRational(Rational rational) {
    return new Matrix(rational);
  }

  public static Matrix fromMatrixContext(MatrixParser.MatrixContext matrixContext, Scope scope) {
    int rowNum = matrixContext.matrix_row().size();
    int columnNum = matrixContext.matrix_row(0).matrix_element().size();

    Rational rationalMatrix[][] = new Rational[rowNum][columnNum];

    for (int r = 0; r < rowNum; ++r) {
      for (int c = 0; c < columnNum; ++c) {
        MatrixParser.Matrix_elementContext currentElement =
            matrixContext.matrix_row(r).matrix_element(c);

        Rational tmp;

        if (currentElement == null) {
          throw new InvalidReadRuntimeError("Illformed matrix rows!");
        }

        if (currentElement.id() != null) {
          tmp = (Rational) scope.getSymbol(currentElement.id().getText()).getValue(Type.RATIONAL);
        } else {
          tmp = Rational.fromRationalContext(currentElement.rational());
        }

        rationalMatrix[r][c] = tmp;

      }
    }

    return new Matrix(rationalMatrix);
  }

  public boolean canBeConverted() {
    return rowNum == 1 && columnNum == 1;
  }

  public Rational[][] getValue() {
    return valueMatrix;
  }

  public double[][] getDoubleValue() {
    double[][] value = new double[getRowNum()][getColumnNum()];

    for (int r = 0; r < getRowNum(); ++r) {
      for (int c = 0; c < getColumnNum(); ++c) {
        value[r][c] = valueMatrix[r][c].getValue();
      }
    }

    return value;
  }

  public Rational getValueAtPosition(int row, int col) {
    return valueMatrix[row][col];
  }

  public int getRowNum() {
    return rowNum;
  }

  public int getColumnNum() {
    return columnNum;
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();

    stringBuilder.append("{\n");
    for (Rational r[] : valueMatrix) {
      stringBuilder.append("  {");

      stringBuilder.append(r[0]);
      for (int i = 1; i < r.length; ++i) {
        stringBuilder.append(", ");
        stringBuilder.append(r[i]);
      }
      stringBuilder.append("}\n");
    }
    stringBuilder.append("}");

    return stringBuilder.toString();
  }
}

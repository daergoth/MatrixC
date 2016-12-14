package com.fordprog.matrix.interpreter.type;

import com.fordprog.matrix.MatrixParser;
import com.fordprog.matrix.interpreter.semantic.Scope;
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

  public static Matrix fromMatrixContext(MatrixParser.MatrixContext matrixContext, Scope scope) {
    int rowNum = matrixContext.matrix_row().size();
    int columnNum = matrixContext.matrix_row(0).matrix_element().size();

    Rational rationalMatrix[][] = new Rational[rowNum][columnNum];

    for (int r = 0; r < rowNum; ++r) {
      for (int c = 0; c < columnNum; ++c) {
        MatrixParser.Matrix_elementContext currentElement =
            matrixContext.matrix_row(r).matrix_element(c);

        Rational tmp;

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
    return valueMatrix.getColumnDimension() == 1 && valueMatrix.getRowDimension() == 1;
  }

  public double[][] getValue() {
    return valueMatrix.getData();
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();

    stringBuilder.append("{\n");
    for (double r[] : valueMatrix.getData()) {
      stringBuilder.append("  {");

      stringBuilder.append(new Rational(r[0]));
      for (int i = 1; i < r.length; ++i) {
        stringBuilder.append(", ");
        stringBuilder.append(new Rational(r[i]));
      }
      stringBuilder.append("}\n");
    }
    stringBuilder.append("}");

    return stringBuilder.toString();
  }
}

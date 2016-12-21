package com.fordprog.matrix.interpreter.execution.stdlib;


import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

import com.fordprog.matrix.interpreter.error.runtime.InvalidOperationParameterRuntimeError;
import com.fordprog.matrix.interpreter.type.Matrix;
import com.fordprog.matrix.interpreter.type.Rational;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;

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

  public Matrix inverse(Matrix m) {
    Rational mValue[][] = new Rational[m.getRowNum()][m.getColumnNum()];
    for (int i = 0; i < m.getRowNum(); i++) {
      mValue[i] = m.getValue()[i].clone();
    }

    int n = mValue.length;
    Rational x[][] = new Rational[n][n];
    Rational b[][] = new Rational[n][n];
    int index[] = new int[n];

    for (int i = 0; i < n; ++i) {
      for (int j = 0; j < n; ++j) {
        if (i == j) {
          b[i][j] = new Rational(BigInteger.ONE, BigInteger.ONE);
        } else {
          b[i][j] = new Rational(BigInteger.ZERO, BigInteger.ONE);
        }
      }
    }

    // Transform the matrix into an upper triangle
    mValue = gaussian(mValue, index);

    // Update the matrix b[i][j] with the ratios stored
    for (int i = 0; i < n - 1; ++i) {
      for (int j = i + 1; j < n; ++j) {
        for (int k = 0; k < n; ++k) {
          b[index[j]][k] =
              rationalOperation.subtract(b[index[j]][k],
                  rationalOperation.multiply(mValue[index[j]][i], b[index[i]][k]));
        }
      }
    }

    // Perform backward substitutions
    for (int i = 0; i < n; ++i) {
      x[n - 1][i] = rationalOperation.divide(b[index[n - 1]][i], mValue[index[n - 1]][n - 1]);
      for (int j = n - 2; j >= 0; --j) {
        x[j][i] = b[index[j]][i];
        for (int k = j + 1; k < n; ++k) {
          x[j][i] =
              rationalOperation
                  .subtract(x[j][i], rationalOperation.multiply(mValue[index[j]][k], x[k][i]));
        }
        x[j][i] = rationalOperation.divide(x[j][i], mValue[index[j]][j]);
      }
    }
    return new Matrix(x);
  }

  private Rational[][] gaussian(Rational[][] m, int[] index) {
    int n = index.length;
    Rational c[] = new Rational[n];

    // Initialize the index
    for (int i = 0; i < n; ++i) {
      index[i] = i;
    }

    // Find the rescaling factors, one from each row
    for (int i = 0; i < n; ++i) {
      Rational c1 = new Rational(BigInteger.ZERO, BigInteger.ONE);
      for (int j = 0; j < n; ++j) {
        Rational c0 = new Rational(abs(m[i][j].getValue()));
        if (logicOperation.greaterThan(c0, c1).isEqual(Rational.TRUE)) {
          c1 = c0;
        }
      }
      c[i] = c1;
    }

    // Search the pivoting element from each column
    int k = 0;
    for (int j = 0; j < n - 1; ++j) {
      Rational pi1 = new Rational(BigInteger.ZERO, BigInteger.ONE);
      for (int i = j; i < n; ++i) {
        Rational pi0 = new Rational(abs(m[index[i]][j].getValue()));
        pi0 = rationalOperation.divide(pi0, c[index[i]]);
        if (logicOperation.greaterThan(pi0, pi1).isEqual(Rational.TRUE)) {
          pi1 = pi0;
          k = i;
        }
      }

      // Interchange rows according to the pivoting order
      int itmp = index[j];
      index[j] = index[k];
      index[k] = itmp;
      for (int i = j + 1; i < n; ++i) {
        Rational pj = rationalOperation.divide(m[index[i]][j], m[index[j]][j]);

        // Record pivoting ratios below the diagonal
        m[index[i]][j] = pj;

        // Modify other elements accordingly
        for (int l = j + 1; l < n; ++l) {
          m[index[i]][l] =
              rationalOperation
                  .subtract(m[index[i]][l], rationalOperation.multiply(pj, m[index[j]][l]));
        }
      }
    }

    return m;
  }

  public Rational determinant(Matrix m) {
    Rational mValue[][] = new Rational[m.getRowNum()][m.getColumnNum()];
    for (int i = 0; i < m.getRowNum(); i++) {
      mValue[i] = m.getValue()[i].clone();
    }

    return determinantArray(mValue);
  }

  private Rational determinantArray(Rational[][] mValue) {
    Rational sum = new Rational(BigInteger.ZERO, BigInteger.ONE);
    Rational s;

    if (mValue.length == 1) {  //bottom case of recursion. size 1 matrix determinant is itself.
      return mValue[0][0];
    }
    for (int i = 0; i < mValue.length; i++) { //finds determinant using row-by-row expansion
      Rational[][] smaller =
          new Rational[mValue.length - 1][mValue.length
              - 1]; //creates smaller matrix- values not in same row, column

      for (int a = 1; a < mValue.length; a++) {
        for (int b = 0; b < mValue.length; b++) {
          if (b < i) {
            smaller[a - 1][b] = mValue[a][b];
          } else if (b > i) {
            smaller[a - 1][b - 1] = mValue[a][b];
          }
        }
      }
      if (i % 2 == 0) { //sign changes based on i
        s = new Rational(BigInteger.ONE, BigInteger.ONE);
      } else {
        s = new Rational(BigInteger.valueOf(-1L), BigInteger.ONE);
        ;
      }

      //recursive step: determinant of larger determined by smaller.
      sum = rationalOperation.add(
          sum,
          rationalOperation.multiply(
              rationalOperation.multiply(
                  s,
                  mValue[0][i]
              ),
              determinantArray(smaller)
          )
      );
    }
    return sum;
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

  public Matrix eigenValues(Matrix m) {

    if (m.getRowNum() == 1 && m.getColumnNum() == 1) {
      return new Matrix(m);
    } else if (m.getRowNum() == 2 && m.getColumnNum() == 2) {
      Rational eValues[][] = new Rational[1][2];

      Rational mValue[][] = new Rational[m.getRowNum()][m.getColumnNum()];
      for (int i = 0; i < m.getRowNum(); i++) {
        mValue[i] = m.getValue()[i].clone();
      }

      Rational a = new Rational(BigInteger.ONE, BigInteger.ONE);

      Rational b = rationalOperation.subtract(
          rationalOperation
              .multiply(new Rational(BigInteger.valueOf(-1L), BigInteger.ONE), mValue[0][0]),
          mValue[1][1]);

      Rational c = rationalOperation.subtract(
          rationalOperation.multiply(mValue[0][0], mValue[1][1]),
          rationalOperation.multiply(mValue[0][1], mValue[1][0])
      );

      Rational temp =
          rationalOperation.subtract(
              rationalOperation.multiply(b, b),
              rationalOperation.multiply(
                  rationalOperation.multiply(
                      new Rational(BigInteger.valueOf(4L), BigInteger.ONE),
                      a
                  ),
                  c
              )
          );

      Rational root = new Rational(sqrt(temp.getValue()));

      Rational minusB =
          new Rational(b.getNumerator().multiply(BigInteger.valueOf(-1L)), b.getDenominator());

      eValues[0][0] =
          rationalOperation.divide(
              rationalOperation.add(minusB, root),
              rationalOperation.multiply(
                  new Rational(BigInteger.valueOf(2L), BigInteger.ONE),
                  a
              )
          );

      eValues[0][1] =
          rationalOperation.divide(
              rationalOperation.subtract(minusB, root),
              rationalOperation.multiply(
                  new Rational(BigInteger.valueOf(2L), BigInteger.ONE),
                  a
              )
          );

      return new Matrix(eValues);
    } else {
      Array2DRowRealMatrix realMatrix = new Array2DRowRealMatrix(m.getDoubleValue());

      double[] eigenValues = new EigenDecomposition(realMatrix).getRealEigenvalues();
      Rational[][] eigenRationals = new Rational[1][eigenValues.length];

      for (int i = 0; i < eigenValues.length; ++i) {
        eigenRationals[0][i] = new Rational(eigenValues[i]);
      }

      return new Matrix(eigenRationals);

    }
  }

  public Matrix eigenVectors(Matrix m) {
    if (m.getRowNum() == 1 && m.getColumnNum() == 1) {
      Rational[][] oneMatrix = {{new Rational(BigInteger.ONE, BigInteger.ONE)}};

      return new Matrix(oneMatrix);
    } else if (m.getRowNum() == 2 && m.getColumnNum() == 2) {

      Rational mValue[][] = new Rational[m.getRowNum()][m.getColumnNum()];
      for (int i = 0; i < m.getRowNum(); i++) {
        mValue[i] = m.getValue()[i].clone();
      }

      Rational eValues[] = eigenValues(m).getValue()[0];
      Rational eVectors[][] = new Rational[2][2];

      Rational zeroRational = new Rational(BigInteger.ZERO, BigInteger.ONE);

      if (mValue[0][1].isEqual(zeroRational) && mValue[1][0].isEqual(zeroRational)) {
        eVectors[0][0] = new Rational(BigInteger.ONE, BigInteger.ONE);
        eVectors[0][1] = new Rational(BigInteger.ZERO, BigInteger.ONE);

        eVectors[1][0] = new Rational(BigInteger.ZERO, BigInteger.ONE);
        eVectors[1][1] = new Rational(BigInteger.ONE, BigInteger.ONE);

        return new Matrix(eVectors);
      }

      if (!mValue[1][0].isEqual(zeroRational)) {
        eVectors[0][0] = rationalOperation.subtract(eValues[0], mValue[1][1]);
        eVectors[0][1] = mValue[1][0];

        eVectors[1][0] = rationalOperation.subtract(eValues[1], mValue[1][1]);
        eVectors[1][1] = mValue[1][0];

        return new Matrix(eVectors);
      }

      if (!mValue[0][1].isEqual(zeroRational)) {
        eVectors[0][0] = mValue[0][1];
        eVectors[0][1] = rationalOperation.subtract(eValues[0], mValue[0][0]);

        eVectors[1][0] = mValue[0][1];
        eVectors[1][1] = rationalOperation.subtract(eValues[1], mValue[0][0]);

        return new Matrix(eVectors);
      }

      return null;

    } else {
      Array2DRowRealMatrix realMatrix = new Array2DRowRealMatrix(m.getDoubleValue());

      EigenDecomposition eigenDecomposition = new EigenDecomposition(realMatrix);

      double[] eigenValues = eigenDecomposition.getRealEigenvalues();
      Rational[][] eigenVectors = new Rational[eigenValues.length][eigenValues.length];

      for (int i = 0; i < eigenValues.length; ++i) {
        double[] realVector = eigenDecomposition.getEigenvector(i).toArray();

        eigenVectors[i][0] = new Rational(realVector[0]);
        eigenVectors[i][1] = new Rational(realVector[1]);
        eigenVectors[i][2] = new Rational(realVector[2]);
      }

      return new Matrix(eigenVectors);
    }
  }


  private boolean isMatchingMatrices(Matrix a, Matrix b) {
    return a.getRowNum() == b.getRowNum() && a.getColumnNum() == b.getColumnNum();
  }

}

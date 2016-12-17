package com.fordprog.matrix.interpreter.type;


import com.fordprog.matrix.MatrixParser;
import com.fordprog.matrix.interpreter.error.runtime.CannotConvertRuntimeError;

import java.math.BigInteger;

public class Rational {

  public static final Rational TRUE = new Rational(BigInteger.ONE, BigInteger.ONE);

  public static final Rational FALSE = new Rational(BigInteger.ZERO, BigInteger.ONE);

  private final BigInteger numerator;

  private final BigInteger denominator;

  public Rational(BigInteger numerator, BigInteger denominator) {
    this.numerator = numerator;
    this.denominator = denominator;
  }

  public Rational(Rational rational) {
    this.numerator = rational.getNumerator().add(BigInteger.ZERO);
    this.denominator = rational.getDenominator().add(BigInteger.ZERO);
  }

  public static Rational fromMatrix(Matrix matrix) {

    if (matrix.canBeConverted()) {
      return new Rational(matrix.getValue()[0][0]);
    }

    throw new CannotConvertRuntimeError("Cannot convert Matrix to Rational");
  }

  public static Rational fromRationalContext(MatrixParser.RationalContext rationalContext) {
    BigInteger numerator = new BigInteger(rationalContext.INTEGER(0).getText());
    BigInteger denominator = new BigInteger(rationalContext.INTEGER(1).getText());

    return new Rational(numerator, denominator);
  }

  public BigInteger getNumerator() {
    return numerator;
  }

  public BigInteger getDenominator() {
    return denominator;
  }

  @Override
  public String toString() {
    return String.valueOf(numerator) + "|" +
        String.valueOf(denominator);
  }
}

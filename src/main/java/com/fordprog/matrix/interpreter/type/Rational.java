package com.fordprog.matrix.interpreter.type;


import com.fordprog.matrix.MatrixParser;
import com.fordprog.matrix.interpreter.error.runtime.CannotConvertRuntimeError;
import org.apache.commons.math3.fraction.BigFraction;

import java.math.BigInteger;

public class Rational {

  public static final Rational TRUE = new Rational(BigInteger.ONE, BigInteger.ONE);

  public static final Rational FALSE = new Rational(BigInteger.ZERO, BigInteger.ONE);

  private final BigInteger numerator;

  private final BigInteger denominator;

  public Rational(BigInteger numerator, BigInteger denominator) {
    if (numerator.signum() == -1 && denominator.signum() == -1) {
      numerator = numerator.multiply(BigInteger.valueOf(-1L));
      denominator = denominator.multiply(BigInteger.valueOf(-1L));
    }

    this.numerator = numerator;
    this.denominator = denominator;
  }

  public Rational(Rational rational) {
    this.numerator = rational.getNumerator().add(BigInteger.ZERO);
    this.denominator = rational.getDenominator().add(BigInteger.ZERO);
  }

  public Rational(double d) {
    if (Math.abs(d) < 1e-15) {
      this.numerator = BigInteger.ZERO;
      this.denominator = BigInteger.ONE;

    } else {
      BigFraction fraction = new BigFraction(d);

      this.numerator = fraction.getNumerator();
      this.denominator = fraction.getDenominator();
    }
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

  public double getValue() {
    return numerator.doubleValue() / denominator.doubleValue();
  }

  public boolean isEqual(Rational other) {
    if (numerator.compareTo(BigInteger.ZERO) == 0) {
      return other.getNumerator().compareTo(BigInteger.ZERO) == 0;
    }

    return numerator.compareTo(other.getNumerator()) == 0
        && denominator.compareTo(other.getDenominator()) == 0;
  }

  @Override
  public String toString() {
    return String.valueOf(numerator) + "|" +
        String.valueOf(denominator);
  }
}

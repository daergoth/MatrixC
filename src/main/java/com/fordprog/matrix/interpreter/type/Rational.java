package com.fordprog.matrix.interpreter.type;


import com.fordprog.matrix.MatrixParser;
import com.fordprog.matrix.interpreter.error.runtime.CannotConvertRuntimeError;
import org.apache.commons.math3.fraction.Fraction;

public class Rational {

  private final Fraction value;

  public Rational(double value) {
    this.value = new Fraction(value);
  }

  public Rational(int numerator, int denominator) {
    this.value = new Fraction(numerator, denominator);
  }

  public Rational(Rational rational) {
    this.value = new Fraction(rational.getNumerator(), rational.getDenominator());
  }

  public static Rational fromMatrix(Matrix matrix) {

    if (matrix.canBeConverted()) {
      return new Rational(matrix.getValue()[0][0]);
    }

    throw new CannotConvertRuntimeError("Cannot convert Matrix to Rational");
  }

  public static Rational fromRationalContext(MatrixParser.RationalContext rationalContext) {
    int numerator = Integer.parseInt(rationalContext.INTEGER(0).getText());
    int denominator = Integer.parseInt(rationalContext.INTEGER(1).getText());

    return new Rational(numerator, denominator);
  }

  public double getAsDouble() {
    return value.doubleValue();
  }

  public int getNumerator() {
    return value.getNumerator();
  }

  public int getDenominator() {
    return value.getDenominator();
  }

  @Override
  public String toString() {
    return String.valueOf(value.getNumerator()) + "|" +
        String.valueOf(value.getDenominator());
  }
}

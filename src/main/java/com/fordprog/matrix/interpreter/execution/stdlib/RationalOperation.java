package com.fordprog.matrix.interpreter.execution.stdlib;

import com.fordprog.matrix.interpreter.type.Rational;

import java.math.BigDecimal;
import java.math.BigInteger;

public class RationalOperation {

  private static RationalOperation instance = new RationalOperation();

  private RationalOperation() {
    // Do nothing...
  }

  public static RationalOperation getInstance() {
    return instance;
  }

  public Rational add(Rational a, Rational b) {
    BigInteger lcm = leastCommonDivisor(a.getDenominator(), b.getDenominator());

    BigInteger aNumerator = a.getNumerator().multiply(lcm.divide(a.getDenominator()));
    BigInteger bNumerator = b.getNumerator().multiply(lcm.divide(b.getDenominator()));

    return new Rational(aNumerator.add(bNumerator), lcm);
  }

  public Rational subtract(Rational a, Rational b) {
    BigInteger lcm = leastCommonDivisor(a.getDenominator(), b.getDenominator());

    BigInteger aNumerator = a.getNumerator().multiply(lcm.divide(a.getDenominator()));
    BigInteger bNumerator = b.getNumerator().multiply(lcm.divide(b.getDenominator()));

    return new Rational(aNumerator.subtract(bNumerator), lcm);
  }

  public Rational multiply(Rational a, Rational b) {
    BigInteger num = a.getNumerator().multiply(b.getNumerator());
    BigInteger den = a.getDenominator().multiply(b.getDenominator());

    if (den.signum() == -1) {
      num = num.multiply(BigInteger.valueOf(-1L));
      den = den.multiply(BigInteger.valueOf(-1L));
    }

    return new Rational(num, den);
  }

  public Rational divide(Rational a, Rational b) {
    BigInteger num = a.getNumerator().multiply(b.getDenominator());
    BigInteger den = a.getDenominator().multiply(b.getNumerator());

    if (den.signum() == -1) {
      num = num.multiply(BigInteger.valueOf(-1L));
      den = den.multiply(BigInteger.valueOf(-1L));
    }

    return new Rational(num, den);
  }

  //Possible precision loss
  public Rational power(Rational a, Rational b) {
    BigDecimal resultNumPart =
        new BigDecimal(a.getNumerator()).pow(b.getNumerator().intValueExact());

    double resultNumerator =
        Math.pow(resultNumPart.doubleValue(), 1.0 / b.getDenominator().doubleValue());

    BigDecimal resultDenPart =
        new BigDecimal(a.getDenominator()).pow(b.getNumerator().intValueExact());

    double resultDenominator =
        Math.pow(resultDenPart.doubleValue(), 1.0 / b.getDenominator().doubleValue());

    return new Rational(BigInteger.valueOf(Math.round(resultNumerator)),
        BigInteger.valueOf(Math.round(resultDenominator)));
  }

  public Rational abs(Rational a) {
    return new Rational(a.getNumerator().abs(), a.getDenominator().abs());
  }

  private BigInteger leastCommonDivisor(BigInteger a, BigInteger b) {
    return ((a.multiply(b)).abs()).divide(a.gcd(b));
  }

}

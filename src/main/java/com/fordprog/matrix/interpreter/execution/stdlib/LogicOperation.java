package com.fordprog.matrix.interpreter.execution.stdlib;


import com.fordprog.matrix.interpreter.type.Rational;

import java.math.BigInteger;

public class LogicOperation {

  private static LogicOperation instance = new LogicOperation();

  private LogicOperation() {
    // Do nothing...
  }

  public static LogicOperation getInstance() {
    return instance;
  }

  public Rational lessThan(Rational a, Rational b) {
    BigInteger lcm = leastCommonDivisor(a.getDenominator(), b.getDenominator());

    BigInteger aNumerator = a.getNumerator().multiply(lcm.divide(a.getDenominator()));
    BigInteger bNumerator = b.getNumerator().multiply(lcm.divide(b.getDenominator()));

    int compare = aNumerator.compareTo(bNumerator);

    return compare == -1 ? Rational.TRUE : Rational.FALSE;
  }

  public Rational greaterThan(Rational a, Rational b) {
    BigInteger lcm = leastCommonDivisor(a.getDenominator(), b.getDenominator());

    BigInteger aNumerator = a.getNumerator().multiply(lcm.divide(a.getDenominator()));
    BigInteger bNumerator = b.getNumerator().multiply(lcm.divide(b.getDenominator()));

    int compare = aNumerator.compareTo(bNumerator);

    return compare == 1 ? Rational.TRUE : Rational.FALSE;
  }

  public Rational lessThanOrEqual(Rational a, Rational b) {
    BigInteger lcm = leastCommonDivisor(a.getDenominator(), b.getDenominator());

    BigInteger aNumerator = a.getNumerator().multiply(lcm.divide(a.getDenominator()));
    BigInteger bNumerator = b.getNumerator().multiply(lcm.divide(b.getDenominator()));

    int compare = aNumerator.compareTo(bNumerator);

    return compare == -1 || compare == 0 ? Rational.TRUE : Rational.FALSE;
  }

  public Rational greaterThanOrEqual(Rational a, Rational b) {
    BigInteger lcm = leastCommonDivisor(a.getDenominator(), b.getDenominator());

    BigInteger aNumerator = a.getNumerator().multiply(lcm.divide(a.getDenominator()));
    BigInteger bNumerator = b.getNumerator().multiply(lcm.divide(b.getDenominator()));

    int compare = aNumerator.compareTo(bNumerator);

    return compare == 1 || compare == 0 ? Rational.TRUE : Rational.FALSE;
  }

  public Rational equalTo(Rational a, Rational b) {
    BigInteger lcm = leastCommonDivisor(a.getDenominator(), b.getDenominator());

    BigInteger aNumerator = a.getNumerator().multiply(lcm.divide(a.getDenominator()));
    BigInteger bNumerator = b.getNumerator().multiply(lcm.divide(b.getDenominator()));

    int compare = aNumerator.compareTo(bNumerator);

    return compare == 0 ? Rational.TRUE : Rational.FALSE;
  }

  private BigInteger leastCommonDivisor(BigInteger a, BigInteger b) {
    return ((a.multiply(b)).abs()).divide(a.gcd(b));
  }

}

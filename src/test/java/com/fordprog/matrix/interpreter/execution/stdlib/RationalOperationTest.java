package com.fordprog.matrix.interpreter.execution.stdlib;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fordprog.matrix.interpreter.type.Rational;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;

public class RationalOperationTest {

  private static RationalOperation rationalOperation;

  @BeforeClass
  public static void setUp() {
    rationalOperation = RationalOperation.getInstance();
  }

  @Test
  public void addWithTwoPositive() {
    // Given
    Rational a = new Rational(BigInteger.ONE, BigInteger.ONE);

    Rational b = new Rational(BigInteger.valueOf(-10L), BigInteger.valueOf(-5L));

    Rational expected = new Rational(BigInteger.valueOf(15L), BigInteger.valueOf(5L));

    // When
    Rational result = rationalOperation.add(a, b);

    // Then
    assertThat(result.getNumerator(), equalTo(expected.getNumerator()));
    assertThat(result.getDenominator(), equalTo(expected.getDenominator()));
  }

  @Test
  public void addWithOnePositiveAndOneNegative() {
    // Given
    Rational a = new Rational(BigInteger.valueOf(2L), BigInteger.valueOf(-1L));

    Rational b = new Rational(BigInteger.valueOf(5L), BigInteger.valueOf(2L));

    Rational expected = new Rational(BigInteger.valueOf(1L), BigInteger.valueOf(2L));

    // When
    Rational result = rationalOperation.add(a, b);

    // Then
    assertThat(result.getNumerator(), equalTo(expected.getNumerator()));
    assertThat(result.getDenominator(), equalTo(expected.getDenominator()));
  }

  @Test
  public void addWithTwoNegative() {
    // Given
    Rational a = new Rational(BigInteger.valueOf(3L), BigInteger.valueOf(-2L));

    Rational b = new Rational(BigInteger.valueOf(6L), BigInteger.valueOf(-3L));

    Rational expected = new Rational(BigInteger.valueOf(-21L), BigInteger.valueOf(6L));

    // When
    Rational result = rationalOperation.add(a, b);

    // Then
    assertThat(result.getNumerator(), equalTo(expected.getNumerator()));
    assertThat(result.getDenominator(), equalTo(expected.getDenominator()));
  }

  @Test
  public void subtractWithTwoPositive() {
    // Given
    Rational a = new Rational(BigInteger.ONE, BigInteger.ONE);

    Rational b = new Rational(BigInteger.valueOf(-10L), BigInteger.valueOf(-5L));

    Rational expected = new Rational(BigInteger.valueOf(-5L), BigInteger.valueOf(5L));

    // When
    Rational result = rationalOperation.subtract(a, b);

    // Then
    assertThat(result.getNumerator(), equalTo(expected.getNumerator()));
    assertThat(result.getDenominator(), equalTo(expected.getDenominator()));
  }

  @Test
  public void subtractWithOnePositiveAndOneNegative() {
    // Given
    Rational a = new Rational(BigInteger.valueOf(-2L), BigInteger.valueOf(1L));

    Rational b = new Rational(BigInteger.valueOf(5L), BigInteger.valueOf(2L));

    Rational expected = new Rational(BigInteger.valueOf(-9L), BigInteger.valueOf(2L));

    // When
    Rational result = rationalOperation.subtract(a, b);

    // Then
    assertThat(result.getNumerator(), equalTo(expected.getNumerator()));
    assertThat(result.getDenominator(), equalTo(expected.getDenominator()));
  }

  @Test
  public void subtractWithTwoNegative() {
    // Given
    Rational a = new Rational(BigInteger.valueOf(-3L), BigInteger.valueOf(2L));

    Rational b = new Rational(BigInteger.valueOf(6L), BigInteger.valueOf(-3L));

    Rational expected = new Rational(BigInteger.valueOf(3L), BigInteger.valueOf(6L));

    // When
    Rational result = rationalOperation.subtract(a, b);

    // Then
    assertThat(result.getNumerator(), equalTo(expected.getNumerator()));
    assertThat(result.getDenominator(), equalTo(expected.getDenominator()));
  }

  @Test
  public void multiplyWithTwoPositive() {
    // Given
    Rational a = new Rational(BigInteger.valueOf(4L), BigInteger.valueOf(6L));

    Rational b = new Rational(BigInteger.valueOf(3L), BigInteger.valueOf(4L));

    Rational expected = new Rational(BigInteger.valueOf(12L), BigInteger.valueOf(24L));

    // When
    Rational result = rationalOperation.multiply(a, b);

    // Then
    assertThat(result.getNumerator(), equalTo(expected.getNumerator()));
    assertThat(result.getDenominator(), equalTo(expected.getDenominator()));
  }

  @Test
  public void multiplyWithOnePositiveAndOneNegative() {
    // Given
    Rational a = new Rational(BigInteger.valueOf(7L), BigInteger.valueOf(6L));

    Rational b = new Rational(BigInteger.valueOf(-4L), BigInteger.valueOf(9L));

    Rational expected = new Rational(BigInteger.valueOf(-28L), BigInteger.valueOf(54L));

    // When
    Rational result = rationalOperation.multiply(a, b);

    // Then
    assertThat(result.getNumerator(), equalTo(expected.getNumerator()));
    assertThat(result.getDenominator(), equalTo(expected.getDenominator()));
  }

  @Test
  public void multiplyWithTwoNegative() {
    // Given
    Rational a = new Rational(BigInteger.valueOf(-8L), BigInteger.valueOf(10L));

    Rational b = new Rational(BigInteger.valueOf(7L), BigInteger.valueOf(-18L));

    Rational expected = new Rational(BigInteger.valueOf(-56L), BigInteger.valueOf(-180L));

    // When
    Rational result = rationalOperation.multiply(a, b);

    // Then
    assertThat(result.getNumerator(), equalTo(expected.getNumerator()));
    assertThat(result.getDenominator(), equalTo(expected.getDenominator()));
  }

  @Test
  public void divideWithTwoPositive() {
    // Given
    Rational a = new Rational(BigInteger.valueOf(4L), BigInteger.valueOf(6L));

    Rational b = new Rational(BigInteger.valueOf(3L), BigInteger.valueOf(4L));

    Rational expected = new Rational(BigInteger.valueOf(16L), BigInteger.valueOf(18L));

    // When
    Rational result = rationalOperation.divide(a, b);

    // Then
    assertThat(result.getNumerator(), equalTo(expected.getNumerator()));
    assertThat(result.getDenominator(), equalTo(expected.getDenominator()));
  }

  @Test
  public void divideWithOnePositiveAndOneNegative() {
    // Given
    Rational a = new Rational(BigInteger.valueOf(7L), BigInteger.valueOf(6L));

    Rational b = new Rational(BigInteger.valueOf(-4L), BigInteger.valueOf(9L));

    Rational expected = new Rational(BigInteger.valueOf(-63L), BigInteger.valueOf(24L));

    // When
    Rational result = rationalOperation.divide(a, b);

    // Then
    assertThat(result.getNumerator(), equalTo(expected.getNumerator()));
    assertThat(result.getDenominator(), equalTo(expected.getDenominator()));
  }

  @Test
  public void divideWithTwoNegative() {
    // Given
    Rational a = new Rational(BigInteger.valueOf(-8L), BigInteger.valueOf(10L));

    Rational b = new Rational(BigInteger.valueOf(7L), BigInteger.valueOf(-18L));

    Rational expected = new Rational(BigInteger.valueOf(144L), BigInteger.valueOf(70L));

    // When
    Rational result = rationalOperation.divide(a, b);

    // Then
    assertThat(result.getNumerator(), equalTo(expected.getNumerator()));
    assertThat(result.getDenominator(), equalTo(expected.getDenominator()));
  }
}

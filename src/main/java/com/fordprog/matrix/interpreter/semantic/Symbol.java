package com.fordprog.matrix.interpreter.semantic;

import com.fordprog.matrix.interpreter.CodePoint;
import com.fordprog.matrix.interpreter.type.Matrix;
import com.fordprog.matrix.interpreter.type.Rational;
import com.fordprog.matrix.interpreter.type.Type;

import java.util.Objects;

public class Symbol {

  private final String identifier;

  private final Type type;

  private final CodePoint firstOccurrence;

  private Object value;

  public Symbol(String identifier, Type type, CodePoint firstOccurrence, Object value) {
    this.identifier = identifier;
    this.type = type;
    this.firstOccurrence = firstOccurrence;
    this.value = value;
  }

  public String getIdentifier() {
    return identifier;
  }

  public Type getType() {
    return type;
  }

  public CodePoint getFirstOccurrence() {
    return firstOccurrence;
  }

  public Object getValue() {
    return value;
  }

  public Object getValue(Type targetType) {
    if (type == targetType) {
      return value;
    }

    if (targetType == Type.MATRIX) {
      return Matrix.fromRational((Rational) value);
    } else {
      return Rational.fromMatrix((Matrix) value);
    }
  }

  public void setValue(Object value, Type sourceType) {
    if (value == null) {
      this.value = value;

      return;
    }

    if (type == sourceType) {
      if (type == Type.RATIONAL) {
        this.value = new Rational((Rational) value);
      } else {
        this.value = new Matrix((Matrix) value);
      }
    } else if (type == Type.MATRIX) {
      this.value = Matrix.fromRational((Rational) value);
    } else if (type == Type.RATIONAL) {
      this.value = Rational.fromMatrix((Matrix) value);
    }

  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private String identifier = null;

    private Type type = null;

    private CodePoint firstOccurrence = null;

    private Object value;

    public Builder identifier(String identifier) {
      this.identifier = identifier;

      return this;
    }

    public Builder type(Type type) {
      this.type = type;

      return this;
    }

    public Builder firstOccurrence(CodePoint firstOccurrence) {
      this.firstOccurrence = firstOccurrence;

      return this;
    }

    public Builder value(Object value) {
      this.value = value;

      return this;
    }

    public Symbol build() {
      return new Symbol(
          Objects.requireNonNull(identifier),
          Objects.requireNonNull(type),
          Objects.requireNonNull(firstOccurrence),
          value
      );
    }
  }


}

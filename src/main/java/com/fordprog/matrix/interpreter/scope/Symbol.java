package com.fordprog.matrix.interpreter.scope;

import com.fordprog.matrix.interpreter.CodePoint;
import com.fordprog.matrix.interpreter.type.Type;

import java.util.Objects;

public class Symbol<T> {

  private final String identifier;

  private final Type type;

  private final CodePoint firstOccurrence;

  private T value;

  public Symbol(String identifier, Type type, CodePoint firstOccurrence, T value) {
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

  public T getValue() {
    return value;
  }

  public void setValue(T value) {
    this.value = value;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder<T> {
    private String identifier = null;

    private Type type = null;

    private CodePoint firstOccurrence = null;

    private T value = null;

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

    public Builder value(T value) {
      this.value = value;

      return this;
    }

    public Symbol<T> build() {
      return new Symbol<T>(
          Objects.requireNonNull(identifier),
          Objects.requireNonNull(type),
          Objects.requireNonNull(firstOccurrence),
          value
      );
    }
  }


}

package com.fordprog.matrix.interpreter.error.runtime;


public class CannotConvertRuntimeError extends RuntimeException {

  public CannotConvertRuntimeError(String message) {
    super(message);
  }
}

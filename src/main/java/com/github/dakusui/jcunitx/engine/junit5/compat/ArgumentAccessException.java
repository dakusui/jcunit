package com.github.dakusui.jcunitx.engine.junit5.compat;

public class ArgumentAccessException extends RuntimeException {
  public ArgumentAccessException(String message, Exception ex) {
    super(message, ex);
  }
}

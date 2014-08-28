package com.github.dakusui.jcunit.exceptions;

public class InvalidTestException extends JCUnitException {
  public InvalidTestException(String message, Throwable t) {
    super(message, t);
  }
}

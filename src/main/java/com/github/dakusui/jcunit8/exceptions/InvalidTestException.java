package com.github.dakusui.jcunit8.exceptions;

/**
 * An exception thrown when an error made by a test
 * writer is detected.
 */
public class InvalidTestException extends JCUnitException {
  public InvalidTestException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidTestException(String message) {
    this(message, null);
  }
}

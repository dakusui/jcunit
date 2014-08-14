package com.github.dakusui.jcunit.exceptions;

/**
 * Thrown when the framework detects an error inside JCUnit's framework, which
 * means usually a framework side's bug.
 */
public class JCUnitFrameworkException extends JCUnitException {
  public JCUnitFrameworkException(String message, Throwable t) {
    super(message, t);
  }
}

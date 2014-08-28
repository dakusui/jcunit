package com.github.dakusui.jcunit.exceptions;

/**
 * An exception thrown when an error made by 'users' of JCUnit user is detected.
 */
public class InvalidParameterException extends JCUnitUserException {
  public InvalidParameterException(String msg, Throwable t) {
    super(msg, t);
  }
}

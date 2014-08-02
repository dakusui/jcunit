package com.github.dakusui.jcunit.exceptions;

import com.github.dakusui.jcunit.exceptions.JCUnitException;

/**
 * An exception thrown when an error made by 'users' of JCUnit user is detected.
 */
public class JCUnitParameterException extends JCUnitUserException {
  public JCUnitParameterException(String msg) {
    super(msg);
  }

  public JCUnitParameterException(String msg, Throwable t) {
    super(msg, t);
  }
}

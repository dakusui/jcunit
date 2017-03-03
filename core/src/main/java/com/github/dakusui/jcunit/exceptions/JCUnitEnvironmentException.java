package com.github.dakusui.jcunit.exceptions;

public class JCUnitEnvironmentException extends JCUnitException {
  /**
   * Serial version UID
   */
  private static final long serialVersionUID = 1434737987358526140L;

  public JCUnitEnvironmentException(String message, @SuppressWarnings("SameParameterValue") Throwable t) {
    super(message, t);
  }

  /**
   * This constructor is reflectively used.
   */
  @SuppressWarnings("unused")
  public JCUnitEnvironmentException(String message) {
    super(message, null);
  }
}

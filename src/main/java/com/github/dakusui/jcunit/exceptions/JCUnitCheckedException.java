package com.github.dakusui.jcunit.exceptions;

public class JCUnitCheckedException extends Exception {
  /**
   * A serial version UID.
   */
  private static final long serialVersionUID = -8729834127235486228L;

  public JCUnitCheckedException() {
  }

  public JCUnitCheckedException(String msg) {
    super(msg);
  }

  public JCUnitCheckedException(Throwable e) {
    super(e);
  }

  public JCUnitCheckedException(String msg, Throwable e) {
    super(msg, e);
  }
}

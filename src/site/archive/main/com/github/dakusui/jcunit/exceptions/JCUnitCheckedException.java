package com.github.dakusui.jcunit.exceptions;

class JCUnitCheckedException extends Exception {
  /**
   * A serial version UID.
   */
  private static final long serialVersionUID = -8729834127235486228L;

  JCUnitCheckedException(@SuppressWarnings("SameParameterValue") String msg, Throwable e) {
    super(msg, e);
  }
}

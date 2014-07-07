package com.github.dakusui.jcunit.exceptions;

public class JCUnitRuntimeException extends RuntimeException {

  /**
   * A serial version UID.
   */
  private static final long serialVersionUID = 5469024857071326421L;

  /**
   * Creates an object of this class.
   *
   * @param message An error message for this object.
   * @param t       A nested <code>throwable</code> object.
   */
  public JCUnitRuntimeException(String message, Throwable t) {
    super(message, t);
  }
}

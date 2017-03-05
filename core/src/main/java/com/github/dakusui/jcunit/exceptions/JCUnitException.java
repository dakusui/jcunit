package com.github.dakusui.jcunit.exceptions;

public abstract class JCUnitException extends RuntimeException {

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
  public JCUnitException(String message, Throwable t) {
    super(formatMessage(message, t), t);
  }

  private static String formatMessage(String message, Throwable t) {
    if (t == null) return message;
    if (message == null) return t.getMessage();
    return message + ":[" + t.getMessage() + "]";
  }
}

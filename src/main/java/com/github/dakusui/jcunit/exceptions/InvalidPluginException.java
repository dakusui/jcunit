package com.github.dakusui.jcunit.exceptions;

/**
 * An exception thrown when an error made by a plugin
 * (TupleGenerator, ConstraintManager, or LevelsProvider)
 * writer is detected.
 */
public class InvalidPluginException extends JCUnitException {
  /**
   * Creates an object of this class.
   *
   * @param message An error message for this object.
   * @param t       A nested <code>throwable</code> object.
   */
  public InvalidPluginException(String message, Throwable t) {
    super(message, t);
  }

  /**
   * Creates an object of this class.
   *
   * @param message An error message for this object.
   */
  public InvalidPluginException(String message) {
    super(message, null);
  }
}

package com.github.dakusui.jcunit.exceptions;

/**
 * An exception thrown when an error made by plugin writer.
 */
public class JCUnitPluginException extends JCUnitException {

  /**
   * A serial version UID.
   */
  private static final long serialVersionUID = 5437069650069162822L;

  public JCUnitPluginException(String message, Throwable t) {
    super(message, t);
  }

  public JCUnitPluginException(String message) {
    super(message);
  }
}

package com.github.dakusui.jcunit.exceptions;

public class JCUnitPluginException extends JCUnitRuntimeException {

  /**
   * A serial version UID.
   */
  private static final long serialVersionUID = 5437069650069162822L;

  public JCUnitPluginException(String message, Throwable t) {
    super(message, t);
  }
}

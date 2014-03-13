package com.github.dakusui.jcunit.exceptions;

public class SymbolNotFoundException extends JCUnitException {

  /**
   * Serial version UID
   */
  private static final long serialVersionUID = 7113721808558087053L;

  public SymbolNotFoundException(String msg, Throwable t) {
    super(msg, t);
  }

}

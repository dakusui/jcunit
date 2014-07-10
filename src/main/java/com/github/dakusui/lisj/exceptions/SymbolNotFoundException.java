package com.github.dakusui.lisj.exceptions;

import com.github.dakusui.jcunit.exceptions.JCUnitCheckedException;

public class SymbolNotFoundException extends JCUnitCheckedException {

  /**
   * Serial version UID
   */
  private static final long serialVersionUID = 7113721808558087053L;
  private final String symbolNames;

  /**
   * Creates an object of this class.
   *
   * @param symbolNames Comma separated symbol names.
   * @param t           A nested exception
   */
  public SymbolNotFoundException(String symbolNames, Throwable t) {
    super(formatSymbolNames(symbolNames), t);
    this.symbolNames = symbolNames;
  }

  static private String formatSymbolNames(String symbolNames) {
    String msg = String
        .format("The symbol(s) '%s' weren't found.", symbolNames);
    return msg;
  }

  /**
   * Returns a string of comma separated symbol names.
   */
  public String getSymbolNames() {
    return this.symbolNames;
  }

}

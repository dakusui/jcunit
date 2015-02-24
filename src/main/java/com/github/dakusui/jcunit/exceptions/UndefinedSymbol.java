package com.github.dakusui.jcunit.exceptions;

public class UndefinedSymbol extends JCUnitCheckedException {
  public UndefinedSymbol(String symbolName) {
    super(String.format("'%s' is not defined.", symbolName), null);
  }

  public UndefinedSymbol() {
    super(null, null);
  }
}

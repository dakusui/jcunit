package com.github.dakusui.jcunitx.engine.junit5.compat;

public class ArgumentConversionException extends RuntimeException {
  public ArgumentConversionException(String s, Exception cause) {
    super(s, cause);
  }

  public ArgumentConversionException(String s) {
    super(s);
  }
}

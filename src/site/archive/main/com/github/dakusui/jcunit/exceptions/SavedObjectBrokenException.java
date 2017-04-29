package com.github.dakusui.jcunit.exceptions;

public class SavedObjectBrokenException extends JCUnitException {
  public SavedObjectBrokenException(String message, @SuppressWarnings("SameParameterValue") Throwable t) {
    super(message, t);
  }
}

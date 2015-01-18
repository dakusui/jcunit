package com.github.dakusui.jcunit.exceptions;

import com.github.dakusui.jcunit.core.Checks;

import java.util.LinkedList;
import java.util.List;

public class InvalidTestException extends JCUnitException {
  private final List<InvalidTestException> nested = new LinkedList<InvalidTestException>();

  public InvalidTestException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidTestException(String message) {
    super(message, null);
  }

  public void addChild(InvalidTestException child) {
    this.nested.add(Checks.checknotnull(child));
  }

  @Override
  public String getMessage() {
    String ret = super.getMessage();
    if (!nested.isEmpty()) {
      ret += ":[";
      boolean isFirst = true;
      for (InvalidTestException each : this.nested) {
        if (!isFirst) {
          ret += ",";
        }
        ret += each.getMessage();
        isFirst = false;
      }
      ret += "]";
    }
    return ret;
  }
}

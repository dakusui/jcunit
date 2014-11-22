package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;

/**
 */
public class Args {
  private final Object[] values;

  Args(Object[] values) {
    Checks.checknotnull(values);
    this.values = values;
  }

  public Object[] values() {
    return this.values;
  }

  public int size() {
    return this.values.length;
  }
}

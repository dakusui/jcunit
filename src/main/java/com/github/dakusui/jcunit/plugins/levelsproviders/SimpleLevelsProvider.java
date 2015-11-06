package com.github.dakusui.jcunit.plugins.levelsproviders;

import com.github.dakusui.jcunit.core.Checks;

public abstract class SimpleLevelsProvider extends LevelsProvider.Base {
  private Object[] values;
  protected Object[] args;

  public SimpleLevelsProvider() {
    ////
    // This provider doesn't take any parameter.
    this.values = Checks.checknotnull(values());
  }

  protected abstract Object[] values();

  @Override
  public int size() {
    return values.length;
  }

  @Override
  public Object get(int n) {
    return values[n];
  }
}

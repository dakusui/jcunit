package com.github.dakusui.jcunit.plugins.levelsproviders;

import com.github.dakusui.jcunit.core.utils.Checks;

public abstract class SimpleLevelsProvider extends LevelsProvider.Base {
  private final Object[] values;

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

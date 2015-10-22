package com.github.dakusui.jcunit.plugins.levelsproviders;

public abstract class MappingLevelsProviderBase<T>
    extends LevelsProviderBase {
  @Override
  public int size() {
    return 1;
  }

  /**
   * This method always returns {@code null} since it will be replaced by the value
   * the {@code FactorMapper#apply} returns.
   *
   */
  @Override
  public T get(int n) {
    return null;
  }
}

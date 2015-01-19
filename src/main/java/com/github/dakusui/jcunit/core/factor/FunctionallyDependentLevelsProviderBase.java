package com.github.dakusui.jcunit.core.factor;

public abstract class FunctionallyDependentLevelsProviderBase<T>
    extends LevelsProviderBase<T>
    implements FunctionallyDependentLevelsProvider<T> {
  @Override
  public int size() {
    return 1;
  }

  @Override
  public T get(int n) {
    return null;
  }
}

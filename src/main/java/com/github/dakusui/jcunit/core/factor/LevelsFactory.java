package com.github.dakusui.jcunit.core.factor;

public interface LevelsFactory<T> {
  public void init(String[] parameters);

  public int size();
  public T get(int index);
}

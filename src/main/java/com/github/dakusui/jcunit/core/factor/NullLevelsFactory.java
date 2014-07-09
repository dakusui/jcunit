package com.github.dakusui.jcunit.core.factor;

public class NullLevelsFactory implements LevelsFactory<Object> {
  public static final LevelsFactory<Object> INSTANCE = new  NullLevelsFactory();
  @Override public void init(String[] parameters) {
  }
  @Override public int size() {
    return 0;
  }
  @Override public Object get(int index) {
    return null;
  }
}

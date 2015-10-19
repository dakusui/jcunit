package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.FactorField;

import java.lang.reflect.Field;

public class DefaultLevelsProvider2 extends LevelsProviderBase<Object> {
  @Override
  protected void init(Field targetField, FactorField annotation, Object[] parameters) {

  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public Object get(int n) {
    return null;
  }
}

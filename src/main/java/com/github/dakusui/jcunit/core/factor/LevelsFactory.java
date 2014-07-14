package com.github.dakusui.jcunit.core.factor;

import java.lang.reflect.Field;

public interface LevelsFactory<T> {
  public void init(Object[] parameters);

  public int size();

  public T get(int index);

  public void setTargetField(Field targetField);

  void setAnnotation(FactorField ann);
}

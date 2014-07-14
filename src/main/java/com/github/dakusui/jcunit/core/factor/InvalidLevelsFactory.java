package com.github.dakusui.jcunit.core.factor;

import java.lang.reflect.Field;

/**
 * This class is provided to represent a default value of {@code FactorField} annotation's
 * {@code levelsFactory}.
 */
public class InvalidLevelsFactory implements LevelsFactory<Object> {
  public static final LevelsFactory<Object> INSTANCE = new InvalidLevelsFactory();

  /**
   * Although a constructor with no parameters of an implementation of {@code LevelsFactory}
   * interface should be public, in this class it is marked private.
   * <p/>
   * This is because this class's intention is to represent an invalid value for {@code levelsFactory}
   * method, which doesn't virtually have a default value.
   */
  private InvalidLevelsFactory() {}

  @Override
  public void init(Object[] parameters) {}

  @Override
  public int size() {
    return 0;
  }

  @Override
  public Object get(int index) {
    return null;
  }

  @Override public void setTargetField(Field targetField) {}

  @Override public void setAnnotation(FactorField ann) {}
}

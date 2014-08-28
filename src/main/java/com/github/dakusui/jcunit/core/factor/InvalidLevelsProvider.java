package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.ParamType;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is provided to represent a default value of {@code FactorField} annotation's
 * {@code levelsFactory}.
 */
public class InvalidLevelsProvider implements LevelsProvider<Object> {
  public static final LevelsProvider<Object> INSTANCE = new InvalidLevelsProvider();

  /**
   * Although a constructor with no parameters of an implementation of {@code LevelsFactory}
   * interface should be public, in this class it is marked private.
   * <p/>
   * This is because this class's intention is to represent an invalid value for {@code levelsFactory}
   * method, which doesn't virtually have a default value.
   */
  private InvalidLevelsProvider() {}

  @Override public ParamType[] parameterTypes() {
    return new ParamType[0];
  }

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

    @Override
    public List<String> getErrorsOnInitialization() {
        return new LinkedList<String>();
    }

}

package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.JCUnitConfigurablePlugin;

import java.lang.reflect.Field;

public interface LevelsProvider<T> extends JCUnitConfigurablePlugin {
  public int size();

  public T get(int index);

  public void setTargetField(Field targetField);

  public void setAnnotation(FactorField ann);
}

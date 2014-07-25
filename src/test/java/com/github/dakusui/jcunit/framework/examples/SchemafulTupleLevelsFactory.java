package com.github.dakusui.jcunit.framework.examples;

import com.github.dakusui.jcunit.core.factor.FactorField;
import com.github.dakusui.jcunit.core.factor.LevelsFactoryBase;

import java.lang.reflect.Field;

public class SchemafulTupleLevelsFactory extends LevelsFactoryBase {
  @Override
  protected void init(Field targetField, FactorField annotation, Object[] parameters) {

  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public Object get(int index) {
    return null;
  }
}

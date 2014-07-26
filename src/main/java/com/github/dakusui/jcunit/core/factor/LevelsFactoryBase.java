package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.Utils;

import java.lang.reflect.Field;

public abstract class LevelsFactoryBase<T> implements LevelsFactory<T> {
  private Field       targetField;
  private FactorField annotation;

  @Override
  final public void init(Object[] parameters) {
    Utils.checknotnull(this.annotation);
    Utils.checknotnull(this.targetField);

    init(this.targetField, this.annotation, parameters);
  }

  protected abstract void init(Field targetField,
      FactorField annotation, Object[] parameters);

  @Override final public void setTargetField(Field targetField) {
    this.targetField = targetField;
  }

  @Override final public void setAnnotation(FactorField ann) {
    this.annotation = ann;
  }
}

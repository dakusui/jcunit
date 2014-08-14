package com.github.dakusui.jcunit.framework.examples;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.factor.LevelsFactoryBase;
import com.github.dakusui.jcunit.generators.TupleGenerator;
import com.github.dakusui.jcunit.generators.TupleGeneratorFactory;

import java.lang.reflect.Field;

public class TupleLevelsFactory extends LevelsFactoryBase {
  TupleGenerator generator = null;
  private Field targetField;

  @Override
  protected void init(Field targetField, FactorField annotation,
      Object[] parameters) {
    Utils.checknotnull(targetField);
    Utils.checknotnull(annotation);
    Utils.checknotnull(parameters);
    this.generator = TupleGeneratorFactory.INSTANCE
        .createTupleGeneratorForField(targetField);
    this.targetField = targetField;
  }

  @Override
  public int size() {
    return (int) this.generator.size();
  }

  @Override
  public Object get(int index) {
    Object ret = Utils.createNewInstanceUsingNoParameterConstructor(this.targetField.getType());
    Utils.initializeObjectWithTuple(ret, this.generator.get(index));
    return ret;
  }
}

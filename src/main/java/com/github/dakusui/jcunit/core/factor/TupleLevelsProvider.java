package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.TestCaseUtils;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.factor.LevelsProviderBase;
import com.github.dakusui.jcunit.generators.TupleGenerator;
import com.github.dakusui.jcunit.generators.TupleGeneratorFactory;

import java.lang.reflect.Field;

public class TupleLevelsProvider extends LevelsProviderBase {
  TupleGenerator generator = null;
  private Field targetField;

  @Override
  protected void init(Field targetField, FactorField annotation,
      Object[] parameters) {
    Checks.checknotnull(targetField);
    Checks.checknotnull(annotation);
    Checks.checknotnull(parameters);
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
    TestCaseUtils.initializeObjectWithTuple(ret, this.generator.get(index));
    return ret;
  }
}
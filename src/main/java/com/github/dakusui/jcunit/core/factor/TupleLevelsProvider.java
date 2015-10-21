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
  private Class<?> targetType;

  @Override
  protected void init(
      Field targetField,
      Object[] parameters) {
    this.generator = TupleGeneratorFactory.INSTANCE
        .createTupleGeneratorForField(targetField);
  }

  @Override
  public int size() {
    return (int) this.generator.size();
  }

  @Override
  public Object get(int index) {
    Object ret = Utils.createNewInstanceUsingNoParameterConstructor(targetType);
    TestCaseUtils.initializeObjectWithTuple(ret, this.generator.get(index));
    return ret;
  }
}

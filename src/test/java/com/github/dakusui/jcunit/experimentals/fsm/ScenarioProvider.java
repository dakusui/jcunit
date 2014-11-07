package com.github.dakusui.jcunit.experimentals.fsm;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.factor.DefaultLevelsProvider;
import com.github.dakusui.jcunit.core.factor.LevelsProvider;
import com.github.dakusui.jcunit.core.factor.LevelsProviderBase;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.generators.TupleGenerator;

import java.lang.reflect.Field;

public class ScenarioProvider<SUT> extends LevelsProviderBase<ScenarioSequence<SUT>> {
  @Override
  protected void init(Field targetField, FactorField annotation, Object[] parameters) {
    TupleGenerator generator = createTupleGenerator(targetField, annotation, parameters);
    for (long i = 0; i < generator.size(); i++) {
      Tuple tuple = generator.get(i);
    }
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public ScenarioSequence<SUT> get(int n) {
    return null;
  }

  private TupleGenerator createTupleGenerator(Field targetField,
      FactorField annotation,
      Object[] parameters) {
    return null;
  }

}

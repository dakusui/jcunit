package com.github.dakusui.jcunit.experimentals.fsm;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.factor.DefaultLevelsProvider;
import com.github.dakusui.jcunit.core.factor.LevelsProvider;
import com.github.dakusui.jcunit.core.factor.LevelsProviderBase;

import java.lang.reflect.Field;

public class ScenarioProvider<SUT> extends LevelsProviderBase<ScenarioSequence<SUT>> {
  @Override
  protected void init(Field targetField, FactorField annotation, Object[] parameters) {

  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public ScenarioSequence<SUT> get(int n) {
    return null;
  }
}

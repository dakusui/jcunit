package com.github.dakusui.jcunit.examples.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.ParamType;
import com.github.dakusui.jcunit.core.factor.FunctionallyDependentLevelsProviderBase;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.fsm.FSMUtils;
import com.github.dakusui.jcunit.fsm.ScenarioSequence;

import java.lang.reflect.Field;

public class FSMLevelsProvider<SUT> extends FunctionallyDependentLevelsProviderBase<ScenarioSequence<SUT>> {
  private ScenarioType scenarioType;
  private String fsmName;

  private static enum ScenarioType {
    setup {
      @Override
      String composeFactorName(String fsmName) {
        return FSMUtils.composeSetUpScenarioName(fsmName);
      }
    },
    main {
      @Override
      String composeFactorName(String fsmName) {
        return FSMUtils.composeMainScenarioName(fsmName);
      }
    };

    abstract String composeFactorName(String fsmName);
  }

  @Override
  protected void init(Field targetField, FactorField annotation, Object[] parameters) {
    this.fsmName = (String) parameters[0];
    this.scenarioType = (ScenarioType) parameters[1];
  }

  @Override
  public ScenarioSequence<SUT> apply(Tuple tuple) {
    Object retObject = tuple.get(this.scenarioType.composeFactorName(this.fsmName));
    Checks.checknotnull(retObject);
    Checks.checkplugin(
        retObject instanceof ScenarioSequence,
        "A generated factor level for '%s' isn't an instance of '%s'",
        this.scenarioType.composeFactorName(this.fsmName),
        ScenarioSequence.class
    );
    // The line above already checked the type of the returned value.
    //noinspection unchecked
    return (ScenarioSequence<SUT>) retObject;
  }

  @Override
  public ParamType[] parameterTypes() {
    return new ParamType[] {
        ParamType.String,
        new ParamType.NonArrayType() {
          @Override
          protected Object parse(String str) {
            return ScenarioType.valueOf(str);
          }
        }
    };
  }
}

package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.ParamType;
import com.github.dakusui.jcunit.core.factor.MappingLevelsProviderBase;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;

import java.lang.reflect.Field;

public class FSMLevelsProvider<SUT> extends MappingLevelsProviderBase<Story<FSMSpec<SUT>, SUT>> {
  private String       fsmName;
  private String       factorName;

  @Override
  protected void init(Field targetField, FactorField annotation, Object[] parameters) {
    this.fsmName = (String) parameters[0];
    this.factorName = targetField.getName();
  }

  @Override
  public String factorName() {
    return factorName;
  }

  @Override
  public Story<FSMSpec<SUT>, SUT> apply(Tuple tuple) {
    String factorName;
    Object retObject = tuple.get(factorName = FSMUtils.composeMainScenarioName(this.fsmName));
    Checks.checknotnull(retObject, "'%s' was not found in tuple. (%s)", factorName, tuple);
    Checks.checkplugin(
        retObject instanceof Story,
        "A generated factor level for '%s' isn't an instance of '%s'",
        factorName,
        ScenarioSequence.class
    );
    // The line above already checked the type of the returned value.
    //noinspection unchecked
    return (Story<FSMSpec<SUT>, SUT>) retObject;
  }

  @Override
  public ParamType[] parameterTypes() {
    return new ParamType[] {
        ParamType.String
    };
  }

  public String getFSMName() {
    return this.fsmName;
  }
}

package com.github.dakusui.jcunit.examples.models.modifiedmealymachine;

import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.ParametersSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;
import com.github.dakusui.jcunit.runners.standard.annotations.As;

public enum CorrectSpec implements FSMSpec<ModifiedMealyMachine> {
  @StateSpec I {
  },
  @StateSpec S {
    public Expectation<ModifiedMealyMachine> getS(
        Expectation.Builder<ModifiedMealyMachine> b) {
      return b.valid(
          S,
          new OutputChecker.ForInteractionHistory(Output.Type.VALUE_RETURNED) {
            @Override
            protected Object computeExpectation(InteractionHistory interactionHistory) {
              return compose(interactionHistory.get("method@param-0"));
            }

            private String compose(Iterable<Object> i) {
              StringBuilder b = new StringBuilder();
              for (Object each : i) {
                b.append(each);
                b.append(":");
              }
              return b.toString();
            }
          }
      ).build();
    }
  };

  @ParametersSpec
  public static final Parameters method = new Parameters.Builder(new Object[][] {
      { "A", "B", "C" }
  }).build();

  @ActionSpec
  public Expectation<ModifiedMealyMachine> method(Expectation.Builder<ModifiedMealyMachine> b, @As("method@param-0") String s) {
    return b.valid(S)
        .build();
  }

  @ActionSpec
  public Expectation<ModifiedMealyMachine> getS(Expectation.Builder<ModifiedMealyMachine> b) {
    return b.invalid().build();
  }

  @Override
  public boolean check(ModifiedMealyMachine sut) {
    return true;
  }
}

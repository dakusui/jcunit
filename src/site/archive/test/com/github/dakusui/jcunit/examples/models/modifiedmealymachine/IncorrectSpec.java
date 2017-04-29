package com.github.dakusui.jcunit.examples.models.modifiedmealymachine;

import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.ParametersSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;

public enum IncorrectSpec implements FSMSpec<ModifiedMealyMachine> {
  @StateSpec I {
  },
  @StateSpec S {
    @ActionSpec
    public Expectation<ModifiedMealyMachine> getS(Expectation.Builder<ModifiedMealyMachine> b) {
      return b.valid(
          S,
          /**
           * You can verify the relationships between input history and output by giving an output
           * checker object to this parameter.
           */
          new OutputChecker.ForInteractionHistory(Output.Type.VALUE_RETURNED) {
            /**
             * @see ForInteractionHistory#computeExpectation(InteractionHistory)
             */
            @Override
            protected Object computeExpectation(InteractionHistory interactionHistory) {
              // You can get an iterable of values associated with a parameter name.
              //
              return compose(interactionHistory.get("method@param-0"));
            }

            private String compose(Iterable<Object> i) {
              StringBuilder b = new StringBuilder();
              for (Object each : i) {
                b.append(each);
                ////
                // Wrong specification introduced on purpose.
                // This should be ":" instead of ";", as you see in "CorrectSpec".
                b.append(";");
              }
              return b.toString();
            }
          }
      ).build();
    }

    @ActionSpec
    public Expectation<ModifiedMealyMachine> getI(Expectation.Builder<ModifiedMealyMachine> b) {
      return b.valid(S).build();
    }
  };

  @ParametersSpec
  public static final Parameters method = new ParametersBuilder(new Object[][] {
      { "A", "B", "C" }
  }).build();

  @ActionSpec
  public Expectation<ModifiedMealyMachine> method(Expectation.Builder<ModifiedMealyMachine> b, String s) {
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

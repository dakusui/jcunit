package com.github.dakusui.jcunit.examples.models.modifiedmealymachine;

import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
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
          new OutputChecker.ForInputHistory(Output.Type.VALUE_RETURNED) {
            /**
             * JCUnit verifies the value output by target method "is" the object returned by this
             * method.
             *
             * @see com.github.dakusui.jcunit.fsm.OutputChecker.ForInputHistory#createMatcher(Object)
             * @param inputHistory
             * @return
             * @throws UndefinedSymbol
             */
            @Override
            protected Object computeExpectation(InputHistory inputHistory) throws UndefinedSymbol {
              return compose(inputHistory.get("method@param-0"));
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
  public static final Parameters method = new Parameters.Builder(new Object[][] {
      { "A", "B", "C" }
  }).build();

  @ActionSpec
  public Expectation<ModifiedMealyMachine> method(Expectation.Builder<ModifiedMealyMachine> b, String s) {
    return b.valid(S)
        .addCollector(new InputHistory.Collector.Default("method"))
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

package com.github.dakusui.jcunit.examples.localconstraint;


import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.ParametersSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
public class LocalConstraintCheckerExample {
  private ScenarioSequence.Observer.Factory observerFactory =  new ScenarioSequence.Observer.Factory() {

    @Override
    public ScenarioSequence.Observer createObserver(String fsmName) {
      return new ScenarioSequence.Observer() {
        @Override
        public <SUT> void startSequence(Story.Stage stage, ScenarioSequence<SUT> seq) {

        }

        @Override
        public <SUT> void run(Story.Stage stage, Scenario<SUT> scenario, SUT sut) {
          if (scenario.when.id().startsWith("equals")) {
            Checks.checkcond(scenario.with.values()[0] != null);
          }
        }

        @Override
        public <SUT> void passed(Story.Stage stage, Scenario<SUT> scenario, SUT sut) {
        }

        @Override
        public <SUT> void failed(Story.Stage stage, Scenario<SUT> scenario, SUT sut, Expectation.Result result) {
        }

        @Override
        public <SUT> void endSequence(Story.Stage stage, ScenarioSequence<SUT> seq) {
        }

        @Override
        public ScenarioSequence.Observer createChild(String childName) {
          assert false;
          return null;
        }
      };
    }
  };

  public enum Spec implements FSMSpec<Object> {
    @StateSpec I {
    };
    @ParametersSpec
    public static final Parameters equals = new Parameters.Builder()
        .beginParameter("another").addValues(null, new Object(), "HELLO").endParameter()
        .setConstraintChecker(new ConstraintChecker.Base() {
          @Override
          public boolean check(Tuple tuple) throws UndefinedSymbol {
            if (tuple.containsKey("another")) {
              return tuple.get("another") != null;
            } else {
              throw new UndefinedSymbol("another");
            }
          }
        })
        .build();

    @ActionSpec
    public Expectation<Object> equals(Expectation.Builder<Object> b, Object o) {
      return b.valid(I, CoreMatchers.is(false)).build();
    }

    @Override
    public boolean check(Object o) {
      return true;
    }
  }

  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<Object, Spec> primary;

  @Test
  public void test() {
    FSMUtils.performStory(this, "primary", new SUTObject(), ScenarioSequence.Observer.Factory.ForSimple.INSTANCE );
  }

  public static class SUTObject {
    @Override
    public boolean equals(Object another) {
//      System.out.println("-->[" + another + "]");
      return false;
    }
  }
}

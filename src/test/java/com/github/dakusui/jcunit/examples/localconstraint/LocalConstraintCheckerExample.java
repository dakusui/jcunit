package com.github.dakusui.jcunit.examples.localconstraint;


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
  public static ScenarioSequence.Observer.Factory observerFactory = ScenarioSequence.Observer.Factory.ForSilent.INSTANCE;

  public enum Spec implements FSMSpec<Object> {
    @StateSpec I {
    };
    @ParametersSpec
    public static final Parameters equals = new Parameters.Builder()
        .beginParameter("another").addValues(null, new Object(), "HELLO").endParameter()
        .setConstraintChecker(new ConstraintChecker.Base() {
          @Override
          public boolean check(Tuple tuple) throws UndefinedSymbol {
            System.out.println("***" + tuple.get("another"));
            return tuple.get("another") != null;
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
    FSMUtils.performStory(this, "primary", new SUTObject(),
        observerFactory);
  }

  public static class SUTObject {
    @Override
    public boolean equals(Object another) {
      System.out.println("-->[" + another + "]");
      return false;
    }
  }
}

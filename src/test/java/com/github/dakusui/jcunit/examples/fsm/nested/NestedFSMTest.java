package com.github.dakusui.jcunit.examples.fsm.nested;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.examples.fsm.flyingspaghettimonster.FlyingSpaghettiMonster;
import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.ParametersSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

public class NestedFSMTest {
  @FactorField(levelsProvider = FSMLevelsProvider.class)
  @FSMLevelsProvider.Parameters(Spec.class)
  public ScenarioSequence<FlyingSpaghettiMonster> main;

  @FactorField(levelsProvider = FSMLevelsProvider.class)
  @FSMLevelsProvider.Parameters(NestedSpec.class)
  public ScenarioSequence<FlyingSpaghettiMonster> nested;

  @Before
  public void before() {

  }

  @Test
  public void test() {
  }

  public enum Spec implements FSMSpec<FlyingSpaghettiMonster> {
    @SuppressWarnings("unused") @StateSpec I {
      @Override
      public boolean check(FlyingSpaghettiMonster flyingSpaghettiMonster) {
        return flyingSpaghettiMonster.isReady();
      }

      @Override
      public Expectation<FlyingSpaghettiMonster> cook(FSM<FlyingSpaghettiMonster> fsm, String dish, String sauce) {
        Checks.checknotnull(fsm);
        return FSMUtils.valid(fsm, COOKED, CoreMatchers.startsWith("Cooking"));
      }
    },
    @StateSpec COOKED {
      @Override
      public boolean check(FlyingSpaghettiMonster flyingSpaghettiMonster) {
        return flyingSpaghettiMonster.isReady();
      }

      @Override
      public Expectation<FlyingSpaghettiMonster> eat(FSM<FlyingSpaghettiMonster> fsm) {
        return FSMUtils.valid(fsm, COOKED, CoreMatchers.containsString("yummy"));
      }

      @Override
      public Expectation<FlyingSpaghettiMonster> cook(FSM<FlyingSpaghettiMonster> fsm, String dish, String sauce) {
        Checks.checknotnull(fsm);
        return FSMUtils.valid(fsm, COOKED, CoreMatchers.startsWith("Cooking"));
      }
    },;


    @ParametersSpec
    public static final Object[][] cook = new Object[][] {
        { "spaghetti", "spaghettini" },
        { "peperoncino", "carbonara", "meat sauce" },
    };

    @ActionSpec
    public Expectation<FlyingSpaghettiMonster> cook(FSM<FlyingSpaghettiMonster> fsm, String pasta, String sauce) {
      return FSMUtils.invalid();
    }

    @ActionSpec
    public Expectation<FlyingSpaghettiMonster> eat(FSM<FlyingSpaghettiMonster> fsm) {
      return FSMUtils.invalid();
    }

    @ActionSpec
    public Expectation<FlyingSpaghettiMonster> train(final FSM<FlyingSpaghettiMonster> fsm) {
      //return FSMUtils.valid(fsm, this, CoreMatchers.instanceOf(FlyingSpaghettiMonster.class));
      final String fsmName = "nested";
      return FSMUtils.valid(fsm, this, new Expectation.Checker.FSM("nested"));
    }
  }

  public enum NestedSpec implements FSMSpec<String> {
    ;

    @Override
    public boolean check(String s) {
      return true;
    }
  }
}

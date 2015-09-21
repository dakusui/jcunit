package com.github.dakusui.jcunit.examples.fsm.nested;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.Param;
import com.github.dakusui.jcunit.examples.fsm.flyingspaghettimonster.FlyingSpaghettiMonster;
import com.github.dakusui.jcunit.examples.fsm.flyingspaghettimonster.NestedFSMBase;
import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.ParametersSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
public class NestedFSMTest {
  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<Spec, FlyingSpaghettiMonster> main;

  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<NestedSpec, String> nested;

  public FlyingSpaghettiMonster sut = new FlyingSpaghettiMonster();

  @Test
  public void test() {
    this.main.perform(
        new FSMContext.Builder().add("main", this.main).add("nested", this.nested).build(),
        this.sut,
        Story.SIMPLE_OBSERVER);
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
        final String fsmName = "nested";

        return FSMUtils.valid(fsm, this, new Expectation.Checker.FSM(fsmName, Story.SIMPLE_OBSERVER));
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
  }

  public enum NestedSpec implements FSMSpec<String> {
    @SuppressWarnings("unused") @StateSpec I {
      @Override
      public boolean check(String s) {
        return "Cooking spaghetti meat sauce".equals(s);
      }
    };

    @ActionSpec
    public Expectation<String> toString(final FSM<String> nestedFSM) {
      return FSMUtils.valid(nestedFSM, this, CoreMatchers.instanceOf(String.class));
    }
  }
}

package com.github.dakusui.jcunit.examples.fsm.nested;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.examples.fsm.flyingspaghettimonster.FlyingSpaghettiMonster;
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
  public Story<Spec, FlyingSpaghettiMonster> primary;

  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<NestedSpec, String> nested;


  @Test
  public void test1() {
    FlyingSpaghettiMonster sut = new FlyingSpaghettiMonster();
    FSMUtils.performStory(this, "primary", sut);
    if (!this.nested.isPerformed()) {
      FSMUtils.performStory(this, "nested", "Cooking spaghetti meat sauce");
    }
  }

  @Test
  public void test2() {
    FlyingSpaghettiMonster sut = new FlyingSpaghettiMonster();
    FSMUtils.performStory(this, "primary", sut, new Story.Observer.Factory.ForSilent());
  }

  public enum Spec implements FSMSpec<FlyingSpaghettiMonster> {
    @SuppressWarnings("unused") @StateSpec I {
      @Override
      public boolean check(FlyingSpaghettiMonster flyingSpaghettiMonster) {
        return !flyingSpaghettiMonster.isReady();
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
        return FSMUtils.valid(fsm, this, new Expectation.Checker.FSM("nested"));
      }
    },;


    @ParametersSpec
    public static final Object[][] cook = new Object[][] {
        { "spaghetti", "spaghettini" },
        { "peperoncino", "carbonara", "meat sauce" },
    };

    @ActionSpec
    public Expectation<FlyingSpaghettiMonster> cook(FSM<FlyingSpaghettiMonster> fsm, String pasta, String sauce) {
      return FSMUtils.invalid(fsm);
    }

    @ActionSpec
    public Expectation<FlyingSpaghettiMonster> eat(FSM<FlyingSpaghettiMonster> fsm) {
      return FSMUtils.invalid(fsm);
    }
  }

  public enum NestedSpec implements FSMSpec<String> {
    @SuppressWarnings("unused") @StateSpec("spaghetti meat sauce")I {
      @Override
      public boolean check(String s) {
        return s != null && s.startsWith("Cooking spaghetti");
      }

      @Override
      public Expectation<String> toString(final FSM<String> nestedFSM) {
        return FSMUtils.valid(nestedFSM, I, CoreMatchers.instanceOf(String.class));
      }
    };

    @ActionSpec
    public abstract Expectation<String> toString(final FSM<String> nestedFSM);
  }
}

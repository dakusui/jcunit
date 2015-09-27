package com.github.dakusui.jcunit.examples.fsm.nested;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.Param;
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
  public enum Spec implements FSMSpec<FlyingSpaghettiMonster> {
    @SuppressWarnings("unused") @StateSpec I {
      @Override
      public boolean check(FlyingSpaghettiMonster flyingSpaghettiMonster) {
        return !flyingSpaghettiMonster.isReady();
      }

      @Override
      public Expectation<FlyingSpaghettiMonster> cook(Expectation.Builder<FlyingSpaghettiMonster> b, String dish, String sauce) {
        return b.valid(COOKED, CoreMatchers.startsWith("Cooking")).build();
      }
    },
    @StateSpec COOKED {
      @Override
      public boolean check(FlyingSpaghettiMonster flyingSpaghettiMonster) {
        return flyingSpaghettiMonster.isReady();
      }

      @Override
      public Expectation<FlyingSpaghettiMonster> eat(Expectation.Builder<FlyingSpaghettiMonster> b) {
        return b.valid(COOKED, CoreMatchers.containsString("yummy")).build();
      }

      @Override
      public Expectation<FlyingSpaghettiMonster> cook(Expectation.Builder<FlyingSpaghettiMonster> b, String dish, String sauce) {
        return b.valid(this, new Expectation.Checker.FSM("nested")).build();
      }
    },;

    @ParametersSpec
    public static final Object[][] cook = new Object[][] {
        { "spaghetti", "spaghettini"/*, "penne" */},
        { "peperoncino",  "meat sauce", "carbonara" },
    };

    @ActionSpec
    public Expectation<FlyingSpaghettiMonster> cook(Expectation.Builder<FlyingSpaghettiMonster> b, String pasta, String sauce) {
      return b.invalid().build();
    }

    @ActionSpec
    public Expectation<FlyingSpaghettiMonster> eat(Expectation.Builder<FlyingSpaghettiMonster> b) {
      return b.invalid().build();
    }
  }

  public enum NestedSpec implements FSMSpec<String> {
    @SuppressWarnings("unused") @StateSpec("spaghetti meat sauce")I {
      @Override
      public boolean check(String s) {
        return s != null && s.startsWith("Cooking spaghetti");
      }

      @Override
      public Expectation<String> toString(Expectation.Builder<String> b) {
        return b.valid(I, CoreMatchers.instanceOf(String.class)).build();
      }
    };

    @ActionSpec
    public abstract Expectation<String> toString(Expectation.Builder<String> b);
  }

  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<Spec, FlyingSpaghettiMonster> primary;

  @FactorField(levelsProvider = FSMLevelsProvider.class, providerParams = { @Param("2") })
  public Story<NestedSpec, String> nested;

  @Test
  public void test1() {
    FlyingSpaghettiMonster sut = new FlyingSpaghettiMonster();
    FSMUtils.performStory(this, "primary", sut, new ScenarioSequence.Observer.Factory.ForSilent());
  }

  @Test
  public void test2() {
    FlyingSpaghettiMonster sut = new FlyingSpaghettiMonster();
    FSMUtils.performStory(this, "primary", sut);
    if (!this.nested.isPerformed()) {
      FSMUtils.performStory(this, "nested", "Cooking spaghetti meat sauce");
    }
  }
}

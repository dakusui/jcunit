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
import com.github.dakusui.jcunit.ututils.UTUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
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
        return b.valid(this, new OutputChecker.FSM("nested")).build();
      }
    },;

    @ParametersSpec
    public static final Parameters cook = new Parameters.Builder(new Object[][]
        {
            {
                "spaghetti", "spaghettini"/*, "penne" */
            },
            {
                "peperoncino", "meat sauce", "carbonara"
            },
        }).build();

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
  public Story<FlyingSpaghettiMonster, Spec> primary;

  @FactorField(levelsProvider = FSMLevelsProvider.class, providerParams = { @Param("2") })
  public Story<String, NestedSpec> nested;

  @Before
  public void before() {
    UTUtils.configureStdIOs();
  }

  @Test
  public void test1() {
    ScenarioSequence.Observer.Factory observerFactory =  ScenarioSequence.Observer.Factory.ForSimple.INSTANCE;
    FlyingSpaghettiMonster sut = new FlyingSpaghettiMonster();
    FSMUtils.performStory(this, "primary", sut, observerFactory);
  }

  @Test
  public void test2() {
    ScenarioSequence.Observer.Factory observerFactory =  new ScenarioSequence.Observer.Factory.ForSimple(UTUtils.out);
    FlyingSpaghettiMonster sut = new FlyingSpaghettiMonster();
    FSMUtils.performStory(this, "primary", sut, observerFactory);
    if (!this.nested.isPerformed()) {
      FSMUtils.performStory(this,
          "nested",
          "Cooking spaghetti meat sauce",
          observerFactory);
    }
  }

  @Test
  public void test3() {
    ScenarioSequence.Observer.Factory observerFactory =  ScenarioSequence.Observer.Factory.ForSilent.INSTANCE;
    FlyingSpaghettiMonster sut = new FlyingSpaghettiMonster();
    FSMUtils.performStory(this, "primary", sut, observerFactory);
    if (!this.nested.isPerformed()) {
      FSMUtils.performStory(this,
          "nested",
          "Cooking spaghetti meat sauce",
          observerFactory);
    }
  }
}

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
  public static ScenarioSequence.Observer.Factory observerFactory = ScenarioSequence.Observer.Factory.ForSilent.INSTANCE;

  public enum Spec implements FSMSpec<FlyingSpaghettiMonster> {
    @SuppressWarnings("unused") @StateSpec I {
      @Override
      public boolean check(FlyingSpaghettiMonster flyingSpaghettiMonster) {
        return !flyingSpaghettiMonster.isReady();
      }

      @Override
      public Interaction<FlyingSpaghettiMonster> cook(Interaction.Builder<FlyingSpaghettiMonster> b, String dish, String sauce) {
        return b.valid(COOKED, CoreMatchers.startsWith("Cooking")).build();
      }
    },
    @StateSpec COOKED {
      @Override
      public boolean check(FlyingSpaghettiMonster flyingSpaghettiMonster) {
        return flyingSpaghettiMonster.isReady();
      }

      @Override
      public Interaction<FlyingSpaghettiMonster> eat(Interaction.Builder<FlyingSpaghettiMonster> b) {
        return b.valid(COOKED, CoreMatchers.containsString("yummy")).build();
      }

      @Override
      public Interaction<FlyingSpaghettiMonster> cook(Interaction.Builder<FlyingSpaghettiMonster> b, String dish, String sauce) {
        return b.valid(this, new Interaction.Checker.FSM("nested")).build();
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
    public Interaction<FlyingSpaghettiMonster> cook(Interaction.Builder<FlyingSpaghettiMonster> b, String pasta, String sauce) {
      return b.invalid().build();
    }

    @ActionSpec
    public Interaction<FlyingSpaghettiMonster> eat(Interaction.Builder<FlyingSpaghettiMonster> b) {
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
      public Interaction<String> toString(Interaction.Builder<String> b) {
        return b.valid(I, CoreMatchers.instanceOf(String.class)).build();
      }
    };

    @ActionSpec
    public abstract Interaction<String> toString(Interaction.Builder<String> b);
  }

  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<FlyingSpaghettiMonster, Spec> primary;

  @FactorField(levelsProvider = FSMLevelsProvider.class, providerParams = { @Param("2") })
  public Story<String, NestedSpec> nested;

  @Test
  public void test1() {
    FlyingSpaghettiMonster sut = new FlyingSpaghettiMonster();
    FSMUtils.performStory(this, "primary", sut, observerFactory);
  }

  @Test
  public void test2() {
    FlyingSpaghettiMonster sut = new FlyingSpaghettiMonster();
    FSMUtils.performStory(this, "primary", sut, observerFactory);
    if (!this.nested.isPerformed()) {
      FSMUtils.performStory(this, "nested", "Cooking spaghetti meat sauce", observerFactory);
    }
  }
}

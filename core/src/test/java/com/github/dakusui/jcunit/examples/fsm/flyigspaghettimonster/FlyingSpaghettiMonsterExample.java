package com.github.dakusui.jcunit.examples.fsm.flyigspaghettimonster;

import com.github.dakusui.jcunit.examples.models.flyingspaghettimonster.FlyingSpaghettiMonster;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.ParametersSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * An example to illustrate how to test a finite state machine in JCUnit.
 */
@RunWith(JCUnit.class)
public class FlyingSpaghettiMonsterExample {
  /**
   * Fields annotated with {@code StateSpec} will be considered states of the FSM.
   * And they must be public, static, final fields, and typed by an enclosing class.
   * Otherwise errors will be reported by JCUnit framework.
   * In this example, {@code Spec} is an enclosing class of {@code I} and {@code COOKED}
   * and they are typed with {@code Spec} because it is a Java {@code enum}, whose
   * members are typed with it.
   * <p/>
   * Methods annotated with {@code ActionSpec} will be considered actions of the FSM.
   * And they must be public, returning {@code Expectation<SUT>}, taking arguments
   * which define the signature of the methods to be tested in the SUT.
   */
  public enum Spec implements FSMSpec<FlyingSpaghettiMonster> {
    @SuppressWarnings("unused") @StateSpec/*("must NOT be ready")*/I {
      @Override
      public boolean check(FlyingSpaghettiMonster flyingSpaghettiMonster) {
        return !flyingSpaghettiMonster.isReady();
      }

      @Override
      public Expectation<FlyingSpaghettiMonster> cook(Expectation.Builder<FlyingSpaghettiMonster> b, String dish, String sauce) {
        return b.valid(COOKED, CoreMatchers.startsWith("Cooking"))
            .build();
      }
    },
    @StateSpec/*("must be ready")*/COOKED {
      @Override
      public boolean check(FlyingSpaghettiMonster flyingSpaghettiMonster) {
        return flyingSpaghettiMonster.isReady();
      }

      @Override
      public Expectation<FlyingSpaghettiMonster> eat(Expectation.Builder<FlyingSpaghettiMonster> b) {
        //return b.valid(COOKED, CoreMatchers.containsString("yummy")).build();
        return b
            .valid(COOKED, CoreMatchers.endsWith("is yummy!"))
            .build();
      }

      @Override
      public Expectation<FlyingSpaghettiMonster> cook(Expectation.Builder<FlyingSpaghettiMonster> b, String dish, String sauce) {
        return b.valid(COOKED, CoreMatchers.startsWith("Cooking"))
            .build();
      }
    },;


    @ParametersSpec
    public static final Parameters cook = new Parameters.Builder(new Object[][] {
        { "spaghetti", "spaghettini" },
        { "peperoncino", "carbonara", "meat sauce" },
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

  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<FlyingSpaghettiMonster, Spec> primary;

  @Test
  public void test1() throws Throwable {
    FlyingSpaghettiMonster sut = new FlyingSpaghettiMonster();
    FSMUtils.performStory(this, "primary", sut, ScenarioSequence.Observer.Factory.ForSimple.INSTANCE);
  }
}

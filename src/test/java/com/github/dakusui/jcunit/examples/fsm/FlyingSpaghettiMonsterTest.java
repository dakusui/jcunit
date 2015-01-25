package com.github.dakusui.jcunit.examples.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.Param;
import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.ParametersSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * An example to illustrate how to test a finite state machine in JCUnit.
 */
@RunWith(JCUnit.class)
public class FlyingSpaghettiMonsterTest {
  /**
   * Fields annotated with {@code StateSpec} will be considered states of the FSM.
   * And they must be public, static, final fields, and typed by the class itself.
   * Otherwise errors will be reported by JCUnit framework.
   * <p/>
   * Methods annotated with {@code ActionSpec} will be actions of the FSM.
   * And they must be public, returning {@code Expectation<SUT>}, taking arguments
   * which define the signature of the methods of SUT.
   */
  public static enum Spec implements FSMSpec<FlyingSpaghettiMonster> {
    @StateSpec I {
      @Override
      public boolean check(FlyingSpaghettiMonster flyingSpaghettiMonster) {
        return flyingSpaghettiMonster.isReady();
      }

      @ActionSpec
      public Expectation<FlyingSpaghettiMonster> cook(FSM<FlyingSpaghettiMonster> fsm, String dish) {
        Checks.checknotnull(fsm);
        return FSMUtils.valid(fsm, COOKED, CoreMatchers.startsWith("Cooking"));
      }
    },
    @StateSpec COOKED {
      @Override
      public boolean check(FlyingSpaghettiMonster flyingSpaghettiMonster) {
        return flyingSpaghettiMonster.isReady();
      }
    },;

    @ActionSpec
    public Expectation<FlyingSpaghettiMonster> cook(FSM<FlyingSpaghettiMonster> fsm, String dish) {
      return FSMUtils.invalid();
    }

    @ParametersSpec
    public static final Object[][] cook = new Object[][] {
        { "spaghetti" },
        { "spaghettini" }
    };

    @ActionSpec
    public Expectation<FlyingSpaghettiMonster> eat(FSM<FlyingSpaghettiMonster> fsm) {
      return FSMUtils.invalid();
    }
  }

  @FactorField(
      levelsProvider = FSMLevelsProvider.class,
      providerParams = {
          @Param("flyingSpaghettiMonster"),
          @Param("setUp")
      })
  public ScenarioSequence<FlyingSpaghettiMonster> setUp;

  @FactorField(
      levelsProvider = FSMLevelsProvider.class,
      providerParams = {
          @Param("flyingSpaghettiMonster"),
          @Param("main")
      })
  public ScenarioSequence<FlyingSpaghettiMonster> main;

  public FlyingSpaghettiMonster sut = new FlyingSpaghettiMonster();

  public static FSM flyingSpaghettiMonster() {
    return FSMUtils.createFSM(Spec.class);
  }

  @Before
  public void before() throws Throwable {
    FSMUtils.performScenarioSequence(this.setUp, this.sut);
  }

  @Test
  public void test() throws Throwable {
    FSMUtils.performScenarioSequence(this.main, this.sut);
  }
}

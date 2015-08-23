package com.github.dakusui.jcunit.examples.fsm.turnstile;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.Param;
import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
public class TurnstileTest {
  public enum Spec implements FSMSpec<Turnstile> {
    @StateSpec I {
      @Override
      public boolean check(Turnstile turnstile) {
        return turnstile.state == Turnstile.State.Locked;
      }
    },
    @StateSpec UNLOCKED {
      @Override
      public boolean check(Turnstile turnstile) {
        return turnstile.state == Turnstile.State.Unlocked;
      }
    };

    @ActionSpec public Expectation<Turnstile> coin(FSM<Turnstile> fsm) {
      return FSMUtils.valid(fsm, UNLOCKED);
    }

    @ActionSpec public Expectation<Turnstile> pass(FSM<Turnstile> fsm) {
      return FSMUtils.invalid(RuntimeException.class);
    }
  }

  @FactorField(
      levelsProvider = FSMLevelsProvider.class,
      providerParams = {
          @Param("turnstileFSM"),
          @Param("setUp"),
      }
  )
  public ScenarioSequence<Turnstile> setUp;

  @FactorField(
      levelsProvider = FSMLevelsProvider.class,
      providerParams = {
          @Param("turnstileFSM"),
          @Param("main"),
      }
  )
  public ScenarioSequence<Turnstile> main;

  public static FSM<Turnstile> turnstileFSM() {
    return FSMUtils.createFSM(Spec.class, 3);
  }

  @Test
  public void test() throws Throwable {
    Turnstile sut = new Turnstile();
    FSMUtils.performScenarioSequence(null, ScenarioSequence.Type.setUp, this.setUp, sut, Story.SILENT_OBSERVER);
    FSMUtils.performScenarioSequence(null, ScenarioSequence.Type.main, this.main, sut, Story.SIMPLE_OBSERVER);
  }
}

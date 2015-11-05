package com.github.dakusui.jcunit.examples.fsm.turnstile;

import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
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

    @ActionSpec
    public Expectation<Turnstile> coin(Expectation.Builder<Turnstile> b) {
      return b.valid(UNLOCKED).build();
    }

    @ActionSpec
    public Expectation<Turnstile> pass(Expectation.Builder<Turnstile> b) {
      return b.invalid(RuntimeException.class).build();
    }
  }

  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<Turnstile, Spec> main;

  @Test
  public void test() throws Throwable {
    Turnstile sut = new Turnstile();
    FSMUtils.performStory(this, "main", sut, ScenarioSequence.Observer.Factory.ForSimple.INSTANCE);
  }
}

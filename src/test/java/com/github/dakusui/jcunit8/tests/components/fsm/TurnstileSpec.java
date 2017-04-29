package com.github.dakusui.jcunit8.tests.components.fsm;

import com.github.dakusui.jcunit.fsm.Expectation;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FsmSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;

public enum TurnstileSpec implements FsmSpec<Turnstile> {
  @StateSpec I {
    @ActionSpec
    public Expectation<Turnstile> coin(Expectation.Builder<Turnstile> builder) {
      return builder.valid(OPENED).build();
    }

    @Override
    public boolean check(Turnstile turnstile) {
      return !turnstile.opened;
    }
  },
  @StateSpec OPENED {
    @ActionSpec
    public Expectation<Turnstile> pass(Expectation.Builder<Turnstile> builder) {
      return builder.valid(I).build();
    }


    @Override
    public boolean check(Turnstile turnstile) {
      return turnstile.opened;
    }
  };

  @ActionSpec
  public Expectation<Turnstile> coin(Expectation.Builder<Turnstile> builder) {
    return builder.invalid(this, IllegalStateException.class).build();
  }

  @ActionSpec
  public Expectation<Turnstile> pass(Expectation.Builder<Turnstile> builder) {
    return builder.invalid(this, IllegalStateException.class).build();
  }
}

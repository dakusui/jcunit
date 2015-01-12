package com.github.dakusui.jcunit.examples.fsm.simplefsmexample;

import com.github.dakusui.jcunit.fsm.SimpleFSMFactory;

/**
* Created by hiroshi on 12/28/14.
*/
public enum ExampleFSM
    implements SimpleFSMFactory.SimpleState<Turnstile> {
  @SimpleFSMFactory.Initial I {
  },
  OK {
    @Override
    public ExampleFSM pass(Turnstile sut) {
      return I;
    }
  },
  NG {
  };

  @Override
  public boolean check(Turnstile sut) {
    return true;
  }

  @SimpleFSMFactory.Transition
  public ExampleFSM insert(Turnstile sut,
      @SimpleFSMFactory.Parameter("coin") int coin) {
    if (coin < 100)
      return this;
    return OK;
  }

  @SimpleFSMFactory.Transition
  public ExampleFSM pass(Turnstile sut) {
    throw new IllegalStateException();
  }

  @SuppressWarnings("unused") // In order not to let IntelliJ complain of 'unused'.
  public static int[] coin(Turnstile sut) {
    return new int[] { 1, 5, 10, 50, 100, 500 };
  }
}

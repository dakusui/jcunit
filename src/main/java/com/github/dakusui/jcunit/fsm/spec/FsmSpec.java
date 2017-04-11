package com.github.dakusui.jcunit.fsm.spec;

import com.github.dakusui.jcunit.fsm.StateChecker;

/**
 * A model interface to model SUT as a finite state machine.
 *
 * You can implement this interface as an 'enum' and it is the easiest way to define a
 * finite state machine in JCUnit.<br/>
 *
 * @param <SUT> Software under test.
 */
public interface FsmSpec<SUT> extends StateChecker<SUT> {
  class Void<SUT> implements FsmSpec<SUT> {
    public static <SUT> FsmSpec<SUT> getInstance() {
      //noinspection unchecked
      return (FsmSpec<SUT>) INSTANCE;
    }
    private static final FsmSpec<?> INSTANCE = new Void();

    @Override
    public boolean check(Object o) {
      return false;
    }
  }
}

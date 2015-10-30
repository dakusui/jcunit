package com.github.dakusui.jcunit.fsm.spec;

import com.github.dakusui.jcunit.fsm.StateChecker;

/**
 * A base interface to model SUT as a finite state machine.
 *
 * You can implement this interface as an 'enum' and it is the easiest way to define a
 * finite state machine in JCUnit.<br/>
 *
 * @param <SUT> Software under test.
 */
public interface FSMSpec<SUT> extends StateChecker<SUT> {
  class Void<SUT> implements FSMSpec<SUT> {
    public static <SUT> FSMSpec<SUT> getInstance() {
      //noinspection unchecked
      return (FSMSpec<SUT>) INSTANCE;
    }
    public static final FSMSpec<Object> INSTANCE = new FSMSpec<Object>() {
      @Override
      public boolean check(Object objet) {
        return false;
      }
    };

    @Override
    public boolean check(Object o) {
      return false;
    }
  }
}

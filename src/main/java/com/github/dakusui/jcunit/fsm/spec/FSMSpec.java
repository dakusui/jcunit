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
  FSMSpec<Object> VOID = new FSMSpec<Object>() {
    @Override
    public boolean check(Object objet) {
      return false;
    }
  };
}

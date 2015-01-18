package com.github.dakusui.jcunit.fsm.spec;

import com.github.dakusui.jcunit.fsm.StateChecker;

/**
 * An interface to define a finite state machine.
 *
 * You can implement this interface as an 'enum' and it is the easiest way to define a
 * finite state machine in JCUnit.<br/>
 *
 *
 *
 * @param <SUT> Software under test.
 */
public interface FSMSpec<SUT> extends StateChecker<SUT> {
}

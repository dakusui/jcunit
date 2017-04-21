package com.github.dakusui.jcunit.fsm.spec;

import com.github.dakusui.jcunit.fsm.StateChecker;

/**
 * A model interface to model SUT as a finite state machine.
 * <p>
 * You can implement this interface as an 'enum' and it is the easiest way to define a
 * finite state machine in JCUnit.<br/>
 *
 * @param <SUT> Software under test.
 */
public interface FsmSpec<SUT> extends StateChecker<SUT> {
}

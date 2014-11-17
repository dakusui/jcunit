package com.github.dakusui.jcunit.fsm;

/**
 * An interface that represents a finite state machine's (FSM) specification.
 *
 * @param <SUT> A software under test.
 */
public interface FSM<SUT> extends Param.Holder {
  State<SUT> initialState();

  State<SUT>[] states();

  Action<SUT>[] actions();
}

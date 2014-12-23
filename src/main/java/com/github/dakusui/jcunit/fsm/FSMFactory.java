package com.github.dakusui.jcunit.fsm;

public interface FSMFactory<SUT> {
  FSM<SUT> createFSM();
}

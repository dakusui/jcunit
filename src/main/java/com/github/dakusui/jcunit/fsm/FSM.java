package com.github.dakusui.jcunit.fsm;

/**
 * An interface that represents a finite state machine's (FSM) specification.
 *
 * @param <SUT> A software under test.
 */
public interface FSM<SUT> {
  public static class EasyFSM<SUT> implements FSM<SUT> {

    @Override
    public State<SUT> initialState() {
      return this.states()[0];
    }

    @Override
    public State<SUT>[] states() {
      return new State[] {
          new State<SUT>() {

            @Override
            public Expectation<SUT> expectation(Action action, Args args) {
              return null;
            }

            @Override
            public boolean matches(SUT sut) {
              return false;
            }
          }
      };
    }

    @Override
    public Action<SUT>[] actions() {
      return new Action[]{

      };
    }
  }
  State<SUT> initialState();

  State<SUT>[] states();

  Action<SUT>[] actions();
}

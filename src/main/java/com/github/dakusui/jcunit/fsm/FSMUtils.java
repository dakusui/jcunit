package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import org.hamcrest.CoreMatchers;

import java.util.List;

public class FSMUtils {
  private FSMUtils() {
  }

  public static <SUT> Expectation<SUT> invalid() {
    return invalid(IllegalStateException.class);
  }

  public static <SUT> Expectation<SUT> invalid(Class<? extends Throwable> klass) {
    return new Expectation(State.VOID, CoreMatchers.instanceOf(klass));
  }

  public static <SUT> Expectation<SUT> invalid(FSM<SUT> fsm, StateChecker<SUT> state, Class<? extends Throwable> klass) {
    Checks.checknotnull(fsm);
    Checks.checknotnull(state);
    return new Expectation(chooseState(fsm, state), CoreMatchers.instanceOf(klass));
  }

  public static <SUT> Expectation<SUT> valid(FSM<SUT> fsm, StateChecker<SUT> state) {
    return new Expectation<SUT>(chooseState(fsm, state), CoreMatchers.anything());
  }

  public static <SUT> Expectation<SUT> valid(FSM<SUT> fsm, StateChecker<SUT> state, Object returnedValue) {
    return new Expectation<SUT>(chooseState(fsm, state), CoreMatchers.is(returnedValue));
  }

  public static <SUT> FSM<SUT> createFSM(Class<? extends FSMSpec> fsmSpecClass) {
    return new FSM<SUT>() {

      @Override
      public State<SUT> initialState() {
        return null;
      }

      @Override
      public List<State<SUT>> states() {
        return null;
      }

      @Override
      public List<Action<SUT>> actions() {
        return null;
      }
    };
  }

  private static <SUT> State<SUT> chooseState(FSM<SUT> fsm, StateChecker<SUT> stateChecker) {
    return null;
  }

  private abstract static class SimpleFSMState<SUT> implements State<SUT> {
    private final StateChecker<SUT> stateChecker;

    SimpleFSMState(StateChecker<SUT> stateChecker) {
      Checks.checknotnull(stateChecker);
      this.stateChecker = stateChecker;
    }

    @Override
    public boolean check(SUT sut) {
      return this.stateChecker.check(sut);
    }
  }
}

package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import org.hamcrest.CoreMatchers;

/**
 * A utility class for FSM (finite state machine) support of JCUnit.
 */
public class FSMUtils {
  static final Class<? extends Object[][]> DOUBLE_ARRAYED_OBJECT_CLASS = Object[][].class;

  private FSMUtils() {
  }

  public static <T, SUT> void performScenarioSequence(T context, ScenarioSequence.Type type, ScenarioSequence<SUT> scenarioSequence, SUT sut, Story.Observer observer) {
    Checks.checknotnull(scenarioSequence);
    Checks.checknotnull(observer);
    observer.startSequence(type, scenarioSequence);
    try {
      for (int i = 0; i < scenarioSequence.size(); i++) {
        Scenario<SUT> each = scenarioSequence.get(i);

        Expectation.Result result = null;
        observer.run(type, each, sut);
        boolean passed = false;
        try {
          Object r = each.perform(context, sut);
          passed = true;
          ////
          // each.perform(sut) didn't throw an exception
          //noinspection unchecked,ThrowableResultOfMethodCallIgnored
          result = each.then().checkReturnedValue(context, sut, r);
        } catch (Throwable t) {
          if (!passed) {
            //noinspection unchecked,ThrowableResultOfMethodCallIgnored
            result = each.then().checkThrownException(context, sut, t);
          } else {
            throw new RuntimeException("Expectation#checkReturnedValue threw an exception. This is considered to be a framework side's bug.", t);
          }
        } finally {
          if (result != null) {
            if (result.isSuccessful())
              observer.passed(type, each, sut);
            else
              observer.failed(type, each, sut);
            result.throwIfFailed();
          }
        }
      }
    } finally {
      observer.endSequence(type, scenarioSequence);
    }
  }

  public static <SUT> Expectation<SUT> invalid() {
    return invalid(IllegalStateException.class);
  }

  public static <SUT> Expectation<SUT> invalid(Class<? extends Throwable> klass) {
    //noinspection unchecked
    return new Expectation(Expectation.Type.EXCEPTION_THROWN, State.VOID, CoreMatchers.instanceOf(klass));
  }

  public static <SUT> Expectation<SUT> invalid(FSM<SUT> fsm, FSMSpec<SUT> state, Class<? extends Throwable> klass) {
    Checks.checknotnull(fsm);
    Checks.checknotnull(state);
    //noinspection unchecked
    return new Expectation(Expectation.Type.EXCEPTION_THROWN, chooseState(fsm, state), CoreMatchers.instanceOf(klass));
  }

  public static <SUT> Expectation<SUT> valid(FSM<SUT> fsm, FSMSpec<SUT> state) {
    return new Expectation<SUT>(Expectation.Type.VALUE_RETURNED, chooseState(fsm, state), CoreMatchers.anything());
  }

  public static <SUT> Expectation<SUT> valid(FSM<SUT> fsm, FSMSpec<SUT> state, Object returnedValue) {
    return valid(fsm, state, CoreMatchers.is(returnedValue));
  }

  public static <SUT> Expectation<SUT> valid(FSM<SUT> fsm, FSMSpec<SUT> state, org.hamcrest.Matcher matcher) {
    return valid(fsm, state, new Expectation.Checker.MatcherBased(matcher));
  }

  public static <SUT> Expectation<SUT> valid(FSM<SUT> fsm, FSMSpec<SUT> state, Expectation.Checker checker) {
    return new Expectation<SUT>(Expectation.Type.VALUE_RETURNED, chooseState(fsm, state), checker);
  }


  public static <SUT> FSM<SUT> createFSM(Class<? extends FSMSpec<SUT>> fsmSpecClass) {
    return createFSM(fsmSpecClass, 2);
  }

  public static <SUT> FSM<SUT> createFSM(Class<? extends FSMSpec<SUT>> fsmSpecClass, int historyLength) {
    return new SimpleFSM<SUT>(fsmSpecClass, historyLength);
  }

  public static String composeMainScenarioName(String fsmName) {
    return String.format("FSM:main:%s", fsmName);
  }

  public static <SUT> String toString(ScenarioSequence<SUT> scenarioSequence) {
    Checks.checknotnull(scenarioSequence);
    Object[] scenarios = new Object[scenarioSequence.size()];
    for (int i = 0; i < scenarios.length; i++) {
      scenarios[i] = scenarioSequence.get(i);
    }
    return String.format("Story:[%s]", Utils.join(",", scenarios));
  }

  private static <SUT> State<SUT> chooseState(FSM<SUT> fsm, StateChecker<SUT> stateChecker) {
    Checks.checknotnull(fsm);
    Checks.checknotnull(stateChecker);
    for (State<SUT> each : fsm.states()) {
      if (((SimpleFSM.SimpleFSMState) each).stateSpec == stateChecker)
        return each;
    }
    Checks.checkcond(false, "No state for '%s' was found.", stateChecker);
    return null;
  }
}

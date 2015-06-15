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

  public static <SUT> void performScenarioSequence(ScenarioSequence.ContextType contextType, ScenarioSequence<SUT> scenarioSequence, SUT sut, Story.Reporter<SUT> reporter) throws Throwable {
    Checks.checknotnull(scenarioSequence);
    Checks.checknotnull(reporter);
    reporter.startStory(contextType, scenarioSequence);
    try {
      for (int i = 0; i < scenarioSequence.size(); i++) {
        Scenario<SUT> each = scenarioSequence.get(i);

        Expectation.Result result = null;
        reporter.run(contextType, each, sut);
        try {
          Object r = each.perform(sut);
          ////
          // each.perform(sut) didn't throw an exception
          //noinspection unchecked,ThrowableResultOfMethodCallIgnored
          result = each.then().checkReturnedValue(, sut, r, );
        } catch (Throwable t) {
          //noinspection unchecked,ThrowableResultOfMethodCallIgnored
          result = each.then().checkThrownException(sut, t);
        } finally {
          if (result != null) {
            if (result.isSuccessful())
              reporter.passed(contextType, each, sut);
            else
              reporter.failed(contextType, each, sut);
            result.throwIfFailed();
          }
        }
      }
    } finally {
      reporter.endStory(contextType, scenarioSequence);
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
    return new Expectation<SUT>(Expectation.Type.VALUE_RETURNED, chooseState(fsm, state), matcher);
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

  public static String composeSetUpScenarioName(String fsmName) {
    return String.format("FSM:setUp:%s", fsmName);
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

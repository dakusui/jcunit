package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.FactorLoader;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import org.hamcrest.CoreMatchers;

import java.lang.reflect.Field;

/**
 * A utility class for FSM (finite state machine) support of JCUnit.
 */
public class FSMUtils {
  static final Class<? extends Object[][]> DOUBLE_ARRAYED_OBJECT_CLASS = Object[][].class;

  private FSMUtils() {
  }

  public static <T, SUT> void performStory(T context, String fsmName, SUT sut) {
    performStory(context, fsmName, sut, new Story.Observer.Factory.ForSimple());
  }

  public static <T, SUT> void performStory(T context, String fsmName, SUT sut, Story.Observer.Factory observerFactory) {
    Checks.checktest(context != null, "Context mustn't be null. Simply give your test object.");
    Checks.checktest(fsmName != null, "fsmName mustn't be null. Give factor field name whose type is Story<SPEC,SUT> of your test object.");
    Checks.checktest(sut != null, "SUT mustn't be null. Give your object to be tested.");
    Checks.checktest(observerFactory != null, "");

    Field storyField = lookupStoryField(context, fsmName);
    validateStoryFiled(storyField);
    Checks.checktest(storyField != null, "The field '%s' was not found or not public in the context '%s'", fsmName, context);

    try {
      Story<? extends FSMSpec<SUT>, SUT> story = (Story<? extends FSMSpec<SUT>, SUT>) storyField.get(context);
      ////
      // If story is null, it only happens because of JCUnit framework bug since JCUnit/JUnit framework
      // should assign an appropriate value to the factor field.
      Checks.checknotnull(story);
      story.perform(context, sut, observerFactory.createObserver(fsmName));
    } catch (IllegalAccessException e) {
      ////
      // This shouldn't happen because storyField is validated in advance.
      Checks.rethrow(e);
    }
  }

  private static void validateStoryFiled(Field storyField) {
    FactorLoader.ValidationResult result = FactorLoader.validate(storyField);
    result.check();
  }

  private static <T> Field lookupStoryField(T context, String fsmName) {
    try {
      return context.getClass().getField(fsmName);
    } catch (NoSuchFieldException e) {
      return null;
    }
  }

  public static <SUT> Expectation<SUT> invalid(FSM<SUT> fsm) {
    return invalid(fsm, IllegalStateException.class);
  }

  public static <SUT> Expectation<SUT> invalid(FSM<SUT> fsm, Class<? extends Throwable> klass) {
    //noinspection unchecked
    return new Expectation(fsm.name(), Expectation.Type.EXCEPTION_THROWN, State.VOID, CoreMatchers.instanceOf(klass));
  }

  public static <SUT> Expectation<SUT> invalid(FSM<SUT> fsm, FSMSpec<SUT> state, Class<? extends Throwable> klass) {
    Checks.checknotnull(fsm);
    Checks.checknotnull(state);
    //noinspection unchecked
    return new Expectation(fsm.name(), Expectation.Type.EXCEPTION_THROWN, chooseState(fsm, state), CoreMatchers.instanceOf(klass));
  }

  public static <SUT> Expectation<SUT> valid(FSM<SUT> fsm, FSMSpec<SUT> state) {
    return new Expectation<SUT>(fsm.name(), Expectation.Type.VALUE_RETURNED, chooseState(fsm, state), CoreMatchers.anything());
  }

  public static <SUT> Expectation<SUT> valid(FSM<SUT> fsm, FSMSpec<SUT> state, Object returnedValue) {
    return valid(fsm, state, CoreMatchers.is(returnedValue));
  }

  public static <SUT> Expectation<SUT> valid(FSM<SUT> fsm, FSMSpec<SUT> state, org.hamcrest.Matcher matcher) {
    return valid(fsm, state, new Expectation.Checker.MatcherBased(matcher));
  }

  public static <SUT> Expectation<SUT> valid(FSM<SUT> fsm, FSMSpec<SUT> state, Expectation.Checker checker) {
    return new Expectation<SUT>(fsm.name(), Expectation.Type.VALUE_RETURNED, chooseState(fsm, state), checker);
  }


  public static <SUT> FSM<SUT> createFSM(String name, Class<? extends FSMSpec<SUT>> fsmSpecClass) {
    return createFSM(name, fsmSpecClass, 2);
  }

  public static <SUT> FSM<SUT> createFSM(String name, Class<? extends FSMSpec<SUT>> fsmSpecClass, int historyLength) {
    return new SimpleFSM<SUT>(name, fsmSpecClass, historyLength);
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

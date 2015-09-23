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

  public static <SUT> FSM<SUT> createFSM(String fsmName, Class<? extends FSMSpec<SUT>> fsmSpecClass) {
    return createFSM(fsmName, fsmSpecClass, 2);
  }

  public static <SUT> FSM<SUT> createFSM(String fsmName, Class<? extends FSMSpec<SUT>> fsmSpecClass, int historyLength) {
    return new SimpleFSM<SUT>(fsmName, fsmSpecClass, historyLength);
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

}

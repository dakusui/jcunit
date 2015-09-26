package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.factor.FactorLoader;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;

import java.lang.reflect.Field;

/**
 * A utility class for FSM (finite state machine) support of JCUnit intended to be
 * used by users of JCUnit.
 */
public class FSMUtils {
  static final Class<? extends Object[][]> DOUBLE_ARRAYED_OBJECT_CLASS = Object[][].class;

  private FSMUtils() {
  }

  /**
   * Invokes {@code FSMUtils#performStory(Object, String, Object, Story.Observer.Factory)}
   * with a new {@code Story.Observer.Factory.ForSimple} object.
   *
   * @see FSMUtils#performStory(Object, String, Object, ScenarioSequence.Observer.Factory)
   */
  public static <T, SUT> void performStory(T context, String fsmName, SUT sut) {
    performStory(context, fsmName, sut, new ScenarioSequence.Observer.Factory.ForSimple());
  }

  /**
   * Performs a story object on {@code sut} in an object {@code context} specified
   * by {@code fsmName}.
   *
   * This method looks up a field whose name is {@code fsmName} and will perform
   * a value of the field as an appropriate {@code Story}. Before performing the story,
   * this method validates the field and its value, e.g., the type is correct or
   * not, value is assigned, etc.
   * If the field doesn't meet the condition an exception will be thrown.
   *
   * It is recommended to use this method to invoke a story rather than directly
   * calling {@code Story#perform} method.
   *
   * @param context          A test object which encloses fsm field(s)
   * @param fsmName          A name of FSM. A field story object is assigned to.
   * @param sut              A object on which the story specified by {@code fsmName}
   *                         will be performed
   * @param observerFactory  A factory that creates an observer to which activities
   *                         done by JCUnit are reported.
   * @param <T>              A test class's type.
   * @param <SUT>            The type of SUT
   */
  public static <T, SUT> void performStory(T context, String fsmName, SUT sut, ScenarioSequence.Observer.Factory observerFactory) {
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
}

package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 * A utility class for FSM (finite state machine) support of JCUnit intended to be
 * used by users of JCUnit.
 */
public class FSMUtils {
  private FSMUtils() {
  }

  public static <T> void resetStories(final T context) {
    for (Story each : Utils.transform(FSMUtils.getStoryFields(Checks.checknotnull(context)), new Utils.Form<Field, Story>() {
      @Override
      public Story apply(Field in) {
        try {
          return (Story) in.get(context);
        } catch (IllegalAccessException e) {
          Checks.checkcond(false, "This code shouldn't be executed, because the field is already validated.");
        }
        return Checks.checknotnull(null);
      }
    })) {
      each.reset();
    }
  }

  /**
   * Invokes {@code FSMUtils#performStory(Object, String, Object, Story.Observer.Factory)}
   * with a new {@code Story.Observer.Factory.ForSimple} object.
   *
   * @see FSMUtils#performStory(Object, String, Object, ScenarioSequence.Observer.Factory)
   */
  public static <T, SUT> void performStory(T context, String fsmName, SUT sut) {
    performStory(context, fsmName, sut, ScenarioSequence.Observer.Factory.ForSimple.INSTANCE);
  }

  /**
   * Performs a story object on {@code sut} in an object {@code context} specified
   * by {@code fsmName}.
   * <p/>
   * This method looks up a field whose name is {@code fsmName} and will perform
   * a value of the field as an appropriate {@code Story}. Before performing the story,
   * this method validates the field and its value, e.g., the type is correct or
   * not, value is assigned, etc.
   * If the field doesn't meet the condition an exception will be thrown.
   * <p/>
   * It is recommended to use this method to invoke a story rather than directly
   * calling {@code Story#perform} method.
   *
   * @param context         A test object which encloses fsm field(s)
   * @param fsmName         A name of FSM. A field story object is assigned to.
   * @param sut             A object on which the story specified by {@code fsmName}
   *                        will be performed
   * @param observerFactory A factory that creates an observer to which activities
   *                        done by JCUnit are reported.
   * @param <T>             A test class's type.
   * @param <SUT>           The type of SUT
   */
  public static <T, SUT> void performStory(T context, String fsmName, SUT sut, ScenarioSequence.Observer.Factory observerFactory) {
    Checks.checktest(context != null, "Context mustn't be null. Simply give your test object.");
    Checks.checktest(fsmName != null, "fsmName mustn't be null. Give factor field name whose type is Story<SPEC,SUT> of your test object.");
    Checks.checktest(sut != null, "SUT mustn't be null. Give your object to be tested.");
    Checks.checktest(observerFactory != null, "");

    Field storyField = lookupStoryField(context, fsmName);
    Checks.checktest(storyField != null, "The field '%s' was not found or not public in the context '%s'", fsmName, context);
    Utils.validateFactorField((storyField)).check();

    ////
    // Ensure stories are reset. By design policy, fields should be immutable.
    // but I couldn't make FSM stories so to implement "nested-FSM" feature.
    // In order to guarantee FSM objects' states are always the same at the
    // beginning of each test (method), I'm calling FSMUtils.resetStories
    // method here. (Issue-#14)
    FSMUtils.resetStories(context);

    try {
      //noinspection unchecked
      Story<SUT, ? extends FSMSpec<SUT>> story = (Story<SUT, ? extends FSMSpec<SUT>>) storyField.get(context);
      ////
      // If story is null, it only happens because of JCUnit framework bug since JCUnit/JUnit framework
      // should assign an appropriate value to the factor field.
      Checks.checktest(story != null, "story parameter must not be null.");
      story.reset();
      story.perform(context, sut, observerFactory.createObserver(fsmName));
    } catch (IllegalAccessException e) {
      ////
      // This shouldn't happen because storyField is validated in advance.
      Checks.rethrow(e);
    }
  }

  /**
   * Returns {@code true} iff {@code f} is FSM field, whose levels provider is
   * a {@code FSMLevelsProvider}. Otherwise {@code false} will be returned.
   *
   * @param f A field to be checked.
   */
  public static boolean isStoryField(Field f) {
    Utils.validateFactorField((Checks.checknotnull(f))).check();
    return FSMLevelsProvider.class.isAssignableFrom(f.getAnnotation(FactorField.class).levelsProvider());
  }

  private static <T> List<Field> getStoryFields(T context) {
    List<Field> ret = new LinkedList<Field>();
    for (Field each : Utils.getAnnotatedFields(context.getClass(), FactorField.class)) {
      if (isStoryField(each)) {
        ret.add(each);
      }
    }
    return ret;
  }

  private static <T> Field lookupStoryField(T context, String fsmName) {
    try {
      return context.getClass().getField(fsmName);
    } catch (NoSuchFieldException e) {
      return null;
    }
  }
}

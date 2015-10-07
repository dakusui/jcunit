package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import org.hamcrest.CoreMatchers;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.assertThat;

/**
 * A utility class for FSM (finite state machine) support of JCUnit intended to be
 * used by users of JCUnit.
 */
public class FSMUtils {

  private FSMUtils() {
  }

  /**
   * Resets all stories in {@code testObject} object.
   *
   * @param testObject A test object whose stories to be reset.
   */
  public static <T> void resetStories(final T testObject) {
    for (Story each : Utils.transform(FSMUtils.getStoryFields(Checks.checknotnull(testObject)), new Utils.Form<Field, Story>() {
      @Override
      public Story apply(Field in) {
        try {
          return (Story) in.get(testObject);
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
  public static <T, SUT> void performStory(T testObject, String fsmName, SUT sut) {
    performStory(testObject, fsmName, sut, ScenarioSequence.Observer.Factory.ForSimple.INSTANCE);
  }

  /**
   * Performs a story object on {@code sut} in an object {@code testObject} specified
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
   * @param testObject      A test object which encloses fsm field(s)
   * @param fsmName         A name of FSM. A field story object is assigned to.
   * @param sut             A object on which the story specified by {@code fsmName}
   *                        will be performed
   * @param observerFactory A factory that creates an observer to which activities
   *                        done by JCUnit are reported.
   * @param <T>             A test class's type.
   * @param <SUT>           The type of SUT
   */
  public static <T, SUT> void performStory(T testObject, String fsmName, SUT sut, ScenarioSequence.Observer.Factory observerFactory) {
    Checks.checktest(testObject != null, "testObject mustn't be null. Simply give your test object.");
    Checks.checktest(fsmName != null, "fsmName mustn't be null. Give factor field name whose type is Story<SPEC,SUT> of your test object.");
    Checks.checktest(sut != null, "SUT mustn't be null. Give your object to be tested.");
    Checks.checktest(observerFactory != null, "");

    ////
    // Ensure stories are reset. By design policy, fields should be immutable.
    // but I couldn't make FSM stories so to implement "nested-FSM" feature.
    // In order to guarantee FSM objects' states are always the same at the
    // beginning of each test (method), I'm calling FSMUtils.resetStories
    // method here. (Issue-#14)
    FSMUtils.resetStories(testObject);

    //noinspection unchecked
    Story<SUT, ? extends FSMSpec<SUT>> story = FSMUtils.lookupStory(testObject, fsmName);
    ////
    // If story is null, it only happens because of JCUnit framework bug since JCUnit/JUnit framework
    // should assign an appropriate value to the factor field.
    Checks.checktest(story != null, "story parameter must not be null.");
    //noinspection unchecked
    Story.Performer.Default.INSTANCE.perform(story, testObject, sut, Synchronizer.DUMMY, observerFactory.createObserver(fsmName));
    ////
    // This path shouldn't be executed because IllegalAccessException is already rethrown
  }

  /**
   * Performs a story object on {@code sut} in an object {@code testObject} specified
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
   * @param testObject A test object which encloses fsm field(s)
   * @param stories    StoryRequest objects each of which holds information
   *                   about which story is performed with what SUT object
   * @param <T>        A test class's type.
   */
  public static <T> void performStoriesConcurrently(final T testObject, Story.Request[] stories) {
    Checks.checktest(testObject != null, "testObject mustn't be null. Simply give your test object.");
    Checks.checktest(stories != null, "Stories mustn't be null. Give factor field name whose type is Story<SPEC,SUT> of your test object.");

    ////
    // Validate story fields in advance
    for (String eachFSMmName : Utils.transform(Arrays.asList(stories), new Utils.Form<Story.Request, String>() {
      @Override
      public String apply(Story.Request in) {
        return in.fsmName;
      }
    })) {
      Field storyField = lookupStoryField(testObject, eachFSMmName);
      Checks.checktest(storyField != null, "The field '%s' was not found or not public in the testObject '%s'", eachFSMmName, testObject);
      Utils.validateFactorField((storyField)).check();
    }

    ////
    // Ensure stories are reset. By design policy, fields should be immutable.
    // but I couldn't make FSM stories so to implement "nested-FSM" feature.
    // In order to guarantee FSM objects' states are always the same at the
    // beginning of each test (method), I'm calling FSMUtils.resetStories
    // method here. (Issue-#14)
    FSMUtils.resetStories(testObject);

    ////
    // Perform scenario sequences concurrently.
    ExecutorService executorService = Executors.newFixedThreadPool(stories.length);
    try {
      @SuppressWarnings("RedundantTypeArguments")
      final Synchronizer synchronizer = new Synchronizer.Builder(
          Utils.<Story.Request, Synchronizable>transform(
              Arrays.asList(stories),
              new Utils.Form<Story.Request, Synchronizable>() {
                @Override
                public Story apply(Story.Request in) {
                  try {
                    return (Story) Checks.checknotnull(FSMUtils.lookupStoryField(testObject, in.fsmName)).get(testObject);
                  } catch (IllegalAccessException e) {
                    Checks.rethrow(e);
                  }
                  throw new RuntimeException();
                }
              })).build();
      //noinspection unchecked
      List<Callable<Boolean>> callables = Utils.transform(
          Arrays.asList(stories), new Utils.Form<Story.Request, Callable<Boolean>>() {
            @Override
            public Callable apply(Story.Request in) {
              //noinspection unchecked
              return in.createCallable(Story.Performer.Default.INSTANCE, synchronizer, testObject);
            }
          });
      for (Future<Boolean> f : executorService.invokeAll(callables)) {
        assertThat(f.get(), CoreMatchers.is(true));
      }
    } catch (InterruptedException e) {
      Checks.rethrow(e);
    } catch (ExecutionException e) {
      Throwable t = e.getCause();
      try {
        throw t;
      } catch (JCUnitException ee) {
        throw ee;
      } catch (RuntimeException ee) {
        throw ee;
      } catch (Error ee) {
        throw ee;
      } catch (Throwable ee) {
        Checks.rethrow(ee);
      }
    } finally {
      executorService.shutdown();
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

  private static <T> List<Field> getStoryFields(T testObject) {
    List<Field> ret = new LinkedList<Field>();
    for (Field each : Utils.getAnnotatedFields(testObject.getClass(), FactorField.class)) {
      if (isStoryField(each)) {
        ret.add(each);
      }
    }
    return ret;
  }

  private static <T> Field lookupStoryField(T testObject, String fsmName) {
    try {
      return testObject.getClass().getField(fsmName);
    } catch (NoSuchFieldException e) {
      return null;
    }
  }

  public static <T, SUT> Story<SUT, ? extends FSMSpec<SUT>> lookupStory(T testObject, String name) {
    Field f = FSMUtils.lookupStoryField(testObject, Checks.checknotnull(name));
    Checks.checktest(f != null, "The field '%s' was not found or not public in the testObject '%s'", name, testObject);
    Utils.validateFactorField((f)).check();

    try {
      //noinspection unchecked
      return (Story<SUT, FSMSpec<SUT>>) f.get(testObject);
    } catch (IllegalAccessException e) {
      ////
      // This shouldn't happen because storyField is validated in advance.
      Checks.rethrow(e, "Unexpected exception '%s' in '%s' is not an accessible field.", name, testObject);
    }
    ////
    // This path shouldn't be executed because IllegalAccessException is already rethrown
    throw new RuntimeException();
  }

  public interface Synchronizable {
  }

  public interface Synchronizer {
    Synchronizer finishAndSynchronize(Synchronizable task);

    void unregister(Synchronizable task);

    Synchronizer DUMMY = new Synchronizer() {
      @Override
      public Synchronizer finishAndSynchronize(Synchronizable task) {
        return this;
      }

      @Override
      public void unregister(Synchronizable task) {
      }
    };

    class Base implements Synchronizer {
      private final Set<Synchronizable> tasks;
      private final Set<Synchronizable> allTasks;
      private       Synchronizer        next;

      public Base(Collection<? extends Synchronizable> tasks) {
        this.tasks = new HashSet<Synchronizable>();
        this.tasks.addAll(Checks.checknotnull(tasks));
        this.allTasks = new LinkedHashSet<Synchronizable>(this.tasks);
      }

      synchronized void finish(Synchronizable task) {
        this.tasks.remove(task);
        this.notifyAll();
      }

      synchronized Synchronizer synchronize() {
        while (!checkIfTasksAreAllDone()) {
          try {
            this.wait();
          } catch (InterruptedException ignored) {
          }
        }
        if (this.next == null) {
          this.next = new Synchronizer.Base(this.allTasks);
        }
        return this.next;
      }

      @Override
      public Synchronizer finishAndSynchronize(Synchronizable task) {
        this.finish(Checks.checknotnull(task));
        return this.synchronize();
      }

      @Override
      public synchronized void unregister(Synchronizable task) {
        Synchronizer cur = this;
        while (cur != null) {
          ((Synchronizer.Base) cur).finish(task);
          cur = ((Base) cur).next;
        }
      }

      private boolean checkIfTasksAreAllDone() {
        return this.tasks.isEmpty();
      }

    }

    class Builder {
      public final List<Synchronizable> tasks;

      Builder(List<Synchronizable> tasks) {
        this.tasks = Utils.dedup(Checks.checknotnull(tasks));
      }

      Synchronizer build() {
        return new Synchronizer.Base(Utils.dedup(Checks.checknotnull(this.tasks)));
      }
    }
  }
}

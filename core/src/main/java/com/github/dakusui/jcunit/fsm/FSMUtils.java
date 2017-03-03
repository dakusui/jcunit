package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.FactorSpace;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArray;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.caengines.Ipo2CoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.caengines.SimpleCoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import org.hamcrest.CoreMatchers;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.assertThat;

/**
 * A utility class for FSM (finite state machine) support of JCUnit intended to be
 * used by users of JCUnit.
 */
public enum FSMUtils {
  ;

  /**
   * Resets all stories in {@code testObject} object.
   *
   * @param testObject A test object whose stories to be reset.
   */
  public static <T> void resetStories(final T testObject) {
    for (Story each : Utils.transform(FSMUtils.getStoryFields(Checks.checknotnull(testObject)), new Utils.Form<Field, Story>() {
      @Override
      public Story apply(Field in) {
        return ReflectionUtils.getFieldValue(testObject, in);
      }
    })) {
      Checks.checknotnull(each, "Probably your test class is not annotated with '@RunWith(JCUnit.class)'.");
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
   * Invokes {@code FSMUtils#performStory(Object, String, Object, Story.Observer.Factory)}
   * with a new {@code Story.Observer.Factory.ForSimple} object.
   *
   * @see FSMUtils#performStory(Object, String, Object, ScenarioSequence.Observer.Factory)
   */
  public static <T, SUT> void performStory(T testObject, String fsmName, SUTFactory<SUT> sut) {
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
   * @param sutFactory      A factory to create an object on which the story specified by {@code fsmName}
   *                        will be performed
   * @param observerFactory A factory that creates an observer to which activities
   *                        done by JCUnit are reported.
   * @param <T>             A test class's type.
   * @param <SUT>           The type of SUT
   */
  public static <T, SUT> void performStory(T testObject, String fsmName, SUTFactory<SUT> sutFactory, ScenarioSequence.Observer.Factory observerFactory) {
    Checks.checktest(testObject != null, "testObject mustn't be null. Simply give your test object.");
    Checks.checktest(fsmName != null, "fsmName mustn't be null. Give factor field name whose type is Story<SPEC,SUT> of your test object.");
    Checks.checktest(sutFactory != null, "SUT mustn't be null. Give your object to be tested.");
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
    Story.Performer.Default.INSTANCE.perform(story, testObject, sutFactory, Synchronizer.DUMMY, observerFactory.createObserver(fsmName));
    ////
    // This path shouldn't be executed because IllegalAccessException is already rethrown
  }

  /**
   * This method has the same effect as the following line.
   * <p/>
   * <pre>
   *   performStory(testObject, fsmName, new SUTFactory.Dummy(sut), observerFactory);
   * </pre>
   *
   * @param testObject      A test object which encloses fsm field(s)
   * @param fsmName         A name of FSM. A field story object is assigned to.
   * @param sut             An object on which the story specified by {@code fsmName}
   *                        will be performed
   * @param observerFactory A factory that creates an observer to which activities
   *                        done by JCUnit are reported.
   * @param <T>             A test class's type.
   * @param <SUT>           The type of SUT
   */
  public static <T, SUT> void performStory(T testObject, String fsmName, SUT sut, ScenarioSequence.Observer.Factory observerFactory) {
    performStory(testObject, fsmName, new SUTFactory.Dummy<SUT>(sut), observerFactory);
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
                  return ReflectionUtils.getFieldValue(testObject, Checks.checknotnull(FSMUtils.lookupStoryField(testObject, in.fsmName)));
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
      throw Checks.wrap(e);
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
        throw Checks.wrap(ee);
      }
    } finally {
      executorService.shutdown();
    }
  }

  /**
   * {@code f} Must be annotated with {@code FactorField}. Its {@code levelsProvider} must be an FSMLevelsProvider.
   * Typed with {@code Story} class.
   *
   * @param f              A field from which an FSM is created.
   * @return Created FSM object
   */
  public static FSM<Object> createFSM(Field f) {
    Checks.checknotnull(f);
    Class<?> clazz = (Class<?>) ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[1];
    //noinspection unchecked
    return createFSM(f.getName(), (Class<? extends FSMSpec<Object>>) clazz);
  }

  /**
   * Create an FSM object from given {@code FSMSpec} and {@code fsmName}
   */
  public static <SUT> FSM<SUT> createFSM(String fsmName, Class<? extends FSMSpec<SUT>> fsmSpecClass) {
    return new FSM.Base<SUT>(fsmName, fsmSpecClass);
  }

  /**
   * Returns a list of {@code Args} objects of a given {@code Action}.
   * An action has {@code Parameters} object, which consists of multiple {@code Factor}s.
   * Since a factor then has multiple possible values (levels), all the possible actual values
   * for an action can become huge number (millions, billions, trillions...).
   *
   * This method returns a practical size of list whose elements are all possible to give the action.
   * A local constraint checking will be executed to guarantee not to make each element valid.
   *
   * This method is used by JCUnit to find a route to a state from an initial state of a given FSM,
   * which is necessary to generate "setUp" scenario sequence.
   */
  public static <SUT> List<Args> possibleArgsList(final Action<SUT> action) {
    if (action.parameters().size() == 0) {
      return Collections.singletonList(new Args(new Object[0]));
    }
    final CoveringArrayEngine engine;
    if (action.parameters().size() == 1) {
      engine = new SimpleCoveringArrayEngine();
    } else {
      engine = new Ipo2CoveringArrayEngine(2);
    }
    final FactorSpace factorSpace = new FactorSpace(
        FactorSpace.convertFactorsIntoSimpleFactorDefs(action.parameters()),
        new ConstraintChecker.Base() {
          @Override
          public boolean check(Tuple tuple) throws UndefinedSymbol {

            return action.parameters().getConstraintChecker().check(tuple);
          }
        }
    );
    final CoveringArray coveringArray = engine.generate(factorSpace);
    return new AbstractList<Args>() {
      @Override
      public Args get(int index) {
        return new Utils.Form<Tuple, Args>() {
          @Override
          public Args apply(final Tuple inTuple) {
            List<Object> tmp = Utils.transform(action.parameters(), new Utils.Form<Factor, Object>() {
              @Override
              public Object apply(Factor inFactor) {
                return inTuple.get(inFactor.name);
              }
            });
            return new Args(tmp.toArray());
          }
        }.apply(coveringArray.get(index));
      }

      @Override
      public int size() {
        return coveringArray.size();
      }
    };
  }

  /**
   * Returns {@code true} iff {@code f} is FSM field, whose levels provider is
   * a {@code FSMLevelsProvider}. Otherwise {@code false} will be returned.
   *
   * @param f A field to be checked.
   */
  private static boolean isStoryField(Field f) {
    return Story.class.isAssignableFrom(f.getType());
  }

  private static <T> List<Field> getStoryFields(T testObject) {
    List<Field> ret = new LinkedList<Field>();
    for (Field each : ReflectionUtils.getFields(testObject.getClass())) {
      if (isStoryField(each)) {
        ret.add(each);
      }
    }
    return ret;
  }

  private static <T> Field lookupStoryField(T testObject, String fsmName) {
    return ReflectionUtils.getField(testObject.getClass(), fsmName);
  }

  static <T, SUT> Story<SUT, ? extends FSMSpec<SUT>> lookupStory(T testObject, String name) {
    Field f = FSMUtils.lookupStoryField(testObject, Checks.checknotnull(name));
    Checks.checktest(f != null, "The field '%s' was not found or not public in the testObject '%s'", name, testObject);
    return ReflectionUtils.getFieldValue(testObject, f);
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

package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * A story comprises setUp and main scenario sequences.
 * Users can perform a story through {@code FSMUtils#performStory}.
 *
 * @param <SPEC> FSMSpec implementation. This information is used reflectively.
 * @param <SUT>  SUT class
 * @see FSMUtils#performStory(Object, String, Object, ScenarioSequence.Observer.Factory)
 */
public class Story<
    SUT, SPEC extends FSMSpec<SUT> // Do not remove to refactor. See Javadoc of this parameter.
    > implements Serializable, FSMUtils.Synchronizable {
  /*
   * A dummy field to suppress a warning for SPEC.
   */
  @SuppressWarnings({ "unused", "FieldCanBeLocal" })
  private           Class<SPEC> klazz;
  private final     String      name;
  transient private boolean     performed;

  private final ScenarioSequence<SUT> setUp;
  private final ScenarioSequence<SUT> main;

  public Story(String name, ScenarioSequence<SUT> setUp, ScenarioSequence<SUT> main) {
    Checks.checknotnull(name);
    Checks.checknotnull(setUp);
    Checks.checknotnull(main);
    this.name = name;
    this.setUp = setUp;
    this.main = main;
    this.klazz = null;
  }

  public void reset() {
    this.performed = false;
  }

  public boolean isPerformed() {
    return this.performed;
  }

  public int hashCode() {
    return this.name.hashCode();
  }

  @Override
  public boolean equals(Object another) {
    if (another instanceof Story) {
      Story anotherStory = ((Story) another);
      return this.name.equals(anotherStory.name) && this.setUp.equals(anotherStory.setUp) && this.main.equals(anotherStory.main);
    }
    return false;
  }

  public static class Request<SUT> {
    public final  String                            fsmName;
    public final  SUT                               sut;
    public final  ScenarioSequence.Observer.Factory observerFactory;

    public Request(String fsmName, SUT sut, ScenarioSequence.Observer.Factory observerFactory) {
      this.fsmName = Checks.checknotnull(fsmName);
      this.sut = Checks.checknotnull(sut);
      this.observerFactory = Checks.checknotnull(observerFactory);
    }

    public void execute(Performer<SUT> performer, FSMUtils.Synchronizer synchronizer, Object context) {
      ScenarioSequence.Observer.Factory observerFactory = this.observerFactory;
      Field storyField = FSMUtils.lookupStoryField(context, this.fsmName);
      Checks.checktest(storyField != null, "The field '%s' was not found or not public in the context '%s'", this.fsmName, context);

      try {
        //noinspection unchecked
        Story story = (Story) storyField.get(context);
        ////
        // If story is null, it only happens because of JCUnit framework bug since JCUnit/JUnit framework
        // should assign an appropriate value to the factor field.
        Checks.checktest(story != null, "story parameter must not be null.");
        //noinspection unchecked
        performer.perform(story, context, sut, synchronizer, observerFactory.createObserver(fsmName));
      } catch (IllegalAccessException e) {
        Checks.rethrow(e);
      }
    }

    public <T> Callable createCallable(final Performer<SUT> performer, final FSMUtils.Synchronizer synchronizer, final T context) {
      return new Callable() {
        @Override
        public Boolean call() {
          //noinspection RedundantCast
          Request.this.execute((Performer<SUT>) performer, synchronizer, context);
          return true;
        }
      };
    }

    public static class ArrayBuilder {
      private final List<Request<?>> requests = new LinkedList<Request<?>>();

      public ArrayBuilder() {
      }

      public <SUT> ArrayBuilder add(String fsmName, SUT sut) {
        requests.add(new Request<SUT>(fsmName, sut, ScenarioSequence.Observer.Factory.ForSimple.INSTANCE));
        return this;
      }

      public Request<?>[] build() {
        return this.requests.toArray(new Request[this.requests.size()]);
      }
    }
  }
  interface Performer<SUT> {
    void perform(Story<SUT, ? extends FSMSpec<SUT>> story, Object context, SUT sut, FSMUtils.Synchronizer synchronizer, ScenarioSequence.Observer observer);

    class Default<SUT> implements Performer<SUT> {
      public static final Performer INSTANCE = new Default();

      @Override
      public void perform(Story<SUT, ? extends FSMSpec<SUT>> story, Object context, SUT sut, FSMUtils.Synchronizer synchronizer, ScenarioSequence.Observer observer) {
        story.performed = true;
        try {
          story.setUp.perform(context, ScenarioSequence.Type.setUp, sut, FSMUtils.Synchronizer.DUMMY, story, observer);
        } finally {
          synchronizer.finishAndSynchronize(story);
        }
        story.main.perform(context, ScenarioSequence.Type.main, sut, synchronizer, story, observer);
      }
    }
  }
}


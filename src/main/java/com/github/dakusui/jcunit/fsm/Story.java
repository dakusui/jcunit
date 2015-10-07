package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * A story comprises SET_UP and MAIN scenario sequences.
 * Users can perform a story through {@code FSMUtils#performStory}.
 *
 * @param <SPEC> FSMSpec implementation. This information is used reflectively.
 * @param <SUT>  SUT class
 * @see FSMUtils#performStory(Object, String, Object, ScenarioSequence.Observer.Factory)
 */
public class Story<
    SUT, SPEC extends FSMSpec<SUT> // Do not remove to refactor. See Javadoc of this parameter.
    >
    implements Serializable,
    FSMUtils.Synchronizable {
  /*
   * A dummy field to suppress a warning for SPEC.
   */
  @SuppressWarnings({ "unused", "FieldCanBeLocal" })
  private           Class<SPEC>              klazz;
  private final     String                   name;
  transient private boolean                  performed;
  transient private Expectation.InputHistory inputHistory;

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
    this.inputHistory = new Expectation.InputHistory.Base();
  }

  public boolean isPerformed() {
    return this.performed;
  }

  public Expectation.InputHistory inputHitory() {
    return this.inputHistory;
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
    public final String                            fsmName;
    public final SUT                               sut;
    public final ScenarioSequence.Observer.Factory observerFactory;

    public Request(String fsmName, SUT sut, ScenarioSequence.Observer.Factory observerFactory) {
      this.fsmName = Checks.checknotnull(fsmName);
      this.sut = Checks.checknotnull(sut);
      this.observerFactory = Checks.checknotnull(observerFactory);
    }

    public <T> void execute(Performer<SUT, T> performer, FSMUtils.Synchronizer synchronizer, T testObject) {
      //noinspection unchecked
      Story<SUT, ? extends FSMSpec<SUT>> story = FSMUtils.lookupStory(testObject, this.fsmName);
      Checks.checktest(story != null, "story parameter must not be null.");
      //noinspection unchecked
      performer.perform(story, testObject, sut, synchronizer, observerFactory.createObserver(fsmName));
    }

    public <T> Callable createCallable(final Performer<SUT, T> performer, final FSMUtils.Synchronizer synchronizer, final T testObject) {
      return new Callable() {
        @Override
        public Boolean call() {
          //noinspection RedundantCast
          Request.this.execute((Performer<SUT, T>) performer, synchronizer, testObject);
          return true;
        }
      };
    }

    public static class ArrayBuilder {
      private final List<Request<?>> requests = new LinkedList<Request<?>>();

      public ArrayBuilder() {
      }

      public <SUT> ArrayBuilder add(String fsmName, SUT sut) {
        return add(fsmName, sut, ScenarioSequence.Observer.Factory.ForSimple.INSTANCE);
      }

      public <SUT> ArrayBuilder add(String fsmName, SUT sut, ScenarioSequence.Observer.Factory observerFactory) {
        requests.add(new Request<SUT>(fsmName, sut, Checks.checknotnull(observerFactory)));
        return this;
      }

      public Request<?>[] build() {
        return this.requests.toArray(new Request[this.requests.size()]);
      }
    }
  }

  interface Performer<SUT, T> {
    void perform(Story<SUT, ? extends FSMSpec<SUT>> story, T testObject, SUT sut, FSMUtils.Synchronizer synchronizer, ScenarioSequence.Observer observer);

    class Default<SUT, T> implements Performer<SUT, T> {
      public static final Performer INSTANCE = new Default();

      @Override
      public void perform(Story<SUT, ? extends FSMSpec<SUT>> story, T testObject, SUT sut, FSMUtils.Synchronizer synchronizer, ScenarioSequence.Observer observer) {
        story.performed = true;
        try {
          story.setUp.perform(testObject, ScenarioSequence.Type.SET_UP, sut, FSMUtils.Synchronizer.DUMMY, story, observer);
        } finally {
          synchronizer.finishAndSynchronize(story);
        }
        story.main.perform(testObject, ScenarioSequence.Type.MAIN, sut, synchronizer, story, observer);
      }
    }
  }
}


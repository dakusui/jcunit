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
    if (!(another instanceof Story)) {
      return false;
    }
    Story anotherStory = ((Story) another);
    return this.name.equals(anotherStory.name) && this.setUp.equals(anotherStory.setUp) && this.main.equals(anotherStory.main);
  }

  /**
   * A stage in which a story is being executed.
   */
  public enum Stage {
    SET_UP {
      @Override
      public Stage next() {
        return MAIN;
      }
    },
    MAIN {
      @Override
      public Stage next() {
        return null;
      }
    },;

    public abstract Stage next();
  }

  public static class Request<SUT> {
    public final String                            fsmName;
    public final SUTFactory<SUT>                   sutFactory;
    public final ScenarioSequence.Observer.Factory observerFactory;

    public Request(String fsmName, SUTFactory<SUT> sutFactory, ScenarioSequence.Observer.Factory observerFactory) {
      this.fsmName = Checks.checknotnull(fsmName);
      this.sutFactory = Checks.checknotnull(sutFactory);
      this.observerFactory = Checks.checknotnull(observerFactory);
    }

    public <T> void execute(Performer<SUT, T> performer, FSMUtils.Synchronizer synchronizer, T testObject) {
      //noinspection unchecked
      Story<SUT, ? extends FSMSpec<SUT>> story = FSMUtils.lookupStory(testObject, this.fsmName);
      Checks.checktest(story != null, "story parameter must not be null.");
      //noinspection unchecked
      performer.perform(story, testObject, sutFactory, synchronizer, observerFactory.createObserver(fsmName));
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

      public <SUT> ArrayBuilder add(String fsmName, SUTFactory<SUT> sutFactory, ScenarioSequence.Observer.Factory observerFactory) {
        requests.add(new Request<SUT>(fsmName, sutFactory, Checks.checknotnull(observerFactory)));
        return this;
      }

      public <SUT> ArrayBuilder add(String fsmName, SUT sut, ScenarioSequence.Observer.Factory observerFactory) {
        requests.add(new Request<SUT>(fsmName, new SUTFactory.Dummy<SUT>(sut), Checks.checknotnull(observerFactory)));
        return this;
      }

      public Request<?>[] build() {
        return this.requests.toArray(new Request[this.requests.size()]);
      }
    }
  }

  interface Performer<SUT, T> {
    void perform(Story<SUT, ? extends FSMSpec<SUT>> story, T testObject, SUTFactory<SUT> sutFactory, FSMUtils.Synchronizer synchronizer, ScenarioSequence.Observer observer);

    class Default<SUT, T> implements Performer<SUT, T> {
      public static final Performer INSTANCE = new Default();

      @Override
      public void perform(Story<SUT, ? extends FSMSpec<SUT>> story, T testObject, SUTFactory<SUT> sutFactory, FSMUtils.Synchronizer synchronizer, ScenarioSequence.Observer observer) {
        story.performed = true;
        Context<SUT, T> context = new Context<SUT, T>(testObject, sutFactory);
        try {
          story.setUp.perform(context, FSMUtils.Synchronizer.DUMMY, story, observer);
          context.next();
        } finally {
          synchronizer.finishAndSynchronize(story);
        }
        story.main.perform(context, synchronizer, story, observer);
      }
    }
  }

  public static class Context<SUT, T> {
    public final T            testObject;
    public final InputHistory inputHistory;
    public final SUT          sut;
    public       Stage        stage;

    public Context(T testObject, SUTFactory<SUT> sutFactory) {
      this(
          Checks.checknotnull(testObject),
          new InputHistory.Base(),
          Checks.checknotnull(sutFactory),
          Stage.SET_UP
      );
    }

    private Context(T testObject, InputHistory inputHistory, SUTFactory<SUT> sutFactory, Stage stage) {
      this(testObject, inputHistory, sutFactory.create(inputHistory), stage);
    }

    public Context(T testObject, InputHistory inputHistory, SUT sut, Stage stage) {
      this.testObject = Checks.checknotnull(testObject);
      this.inputHistory = Checks.checknotnull(inputHistory);
      this.sut = Checks.checknotnull(sut);
      this.stage = Checks.checknotnull(stage);
    }


    public Story<SUT, ? extends FSMSpec<SUT>> lookUpFSMStory(String name) {
      //noinspection unchecked
      return (Story<SUT, ? extends FSMSpec<SUT>>) Checks.checknotnull(
          FSMUtils.lookupStory(
              (T) this.testObject,
              Checks.checknotnull(name)
          ),
          ////
          // If story is null, it only happens because of JCUnit framework bug since JCUnit/JUnit framework
          // should assign an appropriate value to the factor field.
          "A story field '%s' in '%s' shouldn't be null. This field should be set by JCUnit usually",
          name,
          this.testObject
      );
    }

    public void next() {
      Checks.checknotnull(this.stage, "This context (%s) has already finished.", this);
      this.stage = this.stage.next();
    }

    public Stage currentStage() {
      return this.stage;
    }
  }
}

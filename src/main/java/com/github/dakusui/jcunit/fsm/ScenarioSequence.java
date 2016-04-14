package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.JCUnitException;

import java.io.PrintStream;
import java.io.Serializable;

import static com.github.dakusui.jcunit.core.factor.FactorDef.Fsm.*;

/**
 * An interface that represents a sequence of scenarios, each of which consists
 * of "given", "when", and "then" conditions.
 *
 * @param <SUT> A software (class) under test.
 * @see Scenario
 */
public interface ScenarioSequence<SUT> extends Serializable {
  /**
   * Performs this scenario with given {@code sut} object.
   *
   * @param token An object to synchronize scenario sequence execution.
   */
  <T> void perform(Story.Context<SUT, T> context, FSMUtils.Synchronizer synchronizer, FSMUtils.Synchronizable token, Observer observer);

  /**
   * Returns the number of scenarios in this sequence
   */
  int size();

  /**
   * Returns the {@code i}-th scenario in this sequence.
   *
   * @param i history index
   */
  Scenario<SUT> get(int i);

  /**
   * Returns the {@code i}-th state in this sequence.
   * Since {@code null} isn't allowed as a level for state factors, you can tell if the corresponding
   * factor already has a value or not by simply checking this method returns non-null.
   *
   * @param i history index
   */
  State<SUT> state(int i);

  /**
   * Returns the {@code i}-th action in this sequence.
   * Since {@code null} isn't allowed as a level for action factors, you can tell if the corresponding
   * factor already has a value or not by simply checking this method returns non-null.
   *
   * @param i history index
   */
  Action<SUT> action(int i);

  /**
   * Returns {@code j}-th element of {@code i}-th argument list.
   *
   * @param i history index
   * @param j index for argument
   */
  Object arg(int i, int j);

  /**
   * Checks if {@code i}-th argument list has the {@code i}-th element.
   *
   * @param i history index
   * @param j index for argument
   */
  boolean hasArg(int i, int j);

  /**
   * Returns arguments object of {@code i}-th action.
   *
   * @param i history index
   */
  Args args(int i);

  abstract class Base<SUT> implements ScenarioSequence<SUT> {
    public Base() {
    }

    @Override
    public <T> void perform(Story.Context<SUT, T> context, FSMUtils.Synchronizer synchronizer, FSMUtils.Synchronizable token, Observer observer) {
      Checks.checknotnull(context);
      Checks.checknotnull(synchronizer);
      Checks.checknotnull(token);
      Checks.checknotnull(observer);
      Story.Stage stage = context.currentStage();
      observer.startSequence(stage, this);
      SUT sut = context.sut;
      InteractionHistory interactionHistory = context.interactionHistory;
      try {
        for (int i = 0; i < this.size(); i++) {
          Scenario<SUT> each = this.get(i);
          ////
          // Only for the first scenario, make sure SUT is in the 'given' state.
          // We'll see broken test results later on in case it doesn't meet the
          // precondition described as the state, otherwise.
          if (i == 0) {
            if (!each.given.check(sut)) {
              throw new Expectation.Result.Builder("Precondition was not satisfied.")
                  .addFailedReason(StringUtils.format("SUT(%s) isn't in state '%s'", sut, each.given)).build();
            }
          }
          synchronizer = performEachScenario(context, synchronizer, token, observer, stage, sut, interactionHistory, each);
        }
      } finally {
        synchronizer.unregister(token);
        observer.endSequence(stage, this);
      }
    }

    private <T> FSMUtils.Synchronizer performEachScenario(
        Story.Context<SUT, T> context,
        FSMUtils.Synchronizer synchronizer,
        FSMUtils.Synchronizable token,
        Observer observer,
        Story.Stage stage,
        SUT sut,
        InteractionHistory interactionHistory,
        Scenario<SUT> scenario
    ) {
      Expectation.Result result = null;
      observer.run(stage, scenario, sut);
      boolean passed = false;
      try {
        ////
        // Invoke a method in SUT through action corresponding to it.
        // - Invoke the method action in SUT.
        Object r = scenario.perform(sut);
        // 'passed' only means the method in SUT finished without any exceptions.
        // The returned value will be validated by 'checkReturnedValue'. (if
        // an exception is thrown, the thrown exception will be validated by
        // 'checkThrownException'. And if the thrown exception is an expected
        // one, it conforms the spec.)
        passed = true;
        // Author considers that normally application inputs that result
        // in failure should not affect any internal state of a software module.
        // Therefore this try clause should not include the statement above,
        // "each.perform(sut)", because if we do so, the input to the method
        // held by 'each' will be recorded in inputHistory.
        try {
          ////
          // each.perform(sut) didn't throw an exception
          //noinspection unchecked,ThrowableResultOfMethodCallIgnored
          result = scenario.then().checkReturnedValue(context, r, stage, observer);
        } finally {
          ////
          // - Record input history before invoking the action.
          interactionHistory.add(scenario.when, scenario.with);
        }
      } catch (Expectation.Result r) {
        result = r;
      } catch (JCUnitException e) {
        throw e;
      } catch (Throwable t) {
        if (!passed) {
          //noinspection unchecked,ThrowableResultOfMethodCallIgnored
          result = scenario.then().checkThrownException(context, t, observer);
        } else {
          ////
          // Since the previous catch clause ensures the thrown exception is not
          // a JCUnitException, rethrow it without checking.
          throw Checks.wrap(t);
        }
      } finally {
        try {
          ////
          // In case unexpected error is detected, e.g., scenario was not executed
          // because of insufficient privilege to access SUT, result will not be
          // created, result can be null.
          if (result != null) {
            if (result.isSuccessful()) {
              observer.passed(stage, scenario, sut);
            } else {
              observer.failed(stage, scenario, sut, result);
            }
            result.throwIfFailed();
          }
        } finally {
          synchronizer = synchronizer.finishAndSynchronize(token);
        }
      }
      return synchronizer;
    }

    @Override
    public Scenario<SUT> get(int i) {
      Checks.checkcond(i >= 0);
      Checks.checkcond(i < this.size());
      State<SUT> given = this.state(i);
      Action<SUT> when = this.action(i);
      Args with = this.args(i);
      return new Scenario<SUT>(given, when, with);
    }

    @Override
    public int hashCode() {
      return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object another) {
      if (another instanceof ScenarioSequence) {
        ScenarioSequence anotherSequence = (ScenarioSequence) another;
        return PrivateUtils.toString(this).equals(PrivateUtils.toString(anotherSequence));
      }
      return false;
    }

    @Override
    public String toString() {
      return PrivateUtils.toString(this);
    }
  }

  class Empty<SUT> implements ScenarioSequence<SUT> {
    public static <SUT> Empty<SUT> getInstance() {
      //noinspection unchecked
      return (Empty<SUT>) INSTANCE;
    }

    private Empty() {
    }

    @Override
    public void perform(Story.Context context, FSMUtils.Synchronizer synchronizer, FSMUtils.Synchronizable token, Observer observer) {
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public Scenario<SUT> get(int i) {
      throw new IllegalStateException();
    }

    @Override
    public State<SUT> state(int i) {
      throw new IllegalStateException();
    }

    @Override
    public Action<SUT> action(int i) {
      throw new IllegalStateException();
    }

    @Override
    public Object arg(int i, int j) {
      throw new IllegalStateException();
    }

    @Override
    public boolean hasArg(int i, int j) {
      throw new IllegalStateException();
    }

    @Override
    public Args args(int i) {
      throw new IllegalStateException();
    }

    @Override
    public String toString() {
      return PrivateUtils.toString(this);
    }

    @Override
    public int hashCode() {
      return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object another) {
      if (another instanceof ScenarioSequence) {
        ScenarioSequence anotherSequence = (ScenarioSequence) another;
        return PrivateUtils.toString(this).equals(PrivateUtils.toString(anotherSequence));
      }
      return false;
    }

    private static Empty<?> INSTANCE = new Empty<Object>();
  }

  /**
   * Builds a {@code Story} object from a {@code Tuple} using  a given {@code FSMFactors}.
   *
   * @param <SUT> A class of software under test.
   */
  class BuilderFromTuple<SUT> {
    private Tuple  tuple;
    private String fsmName;
    private int    historyLength;

    public BuilderFromTuple() {
    }

    public BuilderFromTuple<SUT> setTuple(Tuple tuple) {
      this.tuple = tuple;
      return this;
    }

    public BuilderFromTuple<SUT> setFSMName(String fsmName) {
      this.fsmName = fsmName;
      return this;
    }

    public BuilderFromTuple<SUT> setHistoryLength(int i) {
      this.historyLength = i;
      return this;
    }

    public ScenarioSequence<SUT> build() {
      Checks.checknotnull(tuple);
      Checks.checknotnull(fsmName);
      Checks.checkcond(historyLength > 0);
      return new ScenarioSequence.Base<SUT>() {
        @Override
        public Scenario<SUT> get(int i) {
          Checks.checkcond(i >= 0);
          Checks.checkcond(i < this.size());
          State<SUT> given = this.state(i);
          Action<SUT> when = this.action(i);
          Args with = this.args(i);
          return new Scenario<SUT>(given, when, with);
        }

        @Override
        public State<SUT> state(int i) {
          Checks.checkcond(i >= 0);
          Checks.checkcond(i < this.size());
          //noinspection unchecked
          return (State<SUT>) tuple.get(stateName(fsmName, i));
        }

        @Override
        public Action<SUT> action(int i) {
          Checks.checkcond(i >= 0);
          Checks.checkcond(i < this.size());
          //noinspection unchecked
          return (Action<SUT>) tuple.get(actionName(fsmName, i));
        }

        @Override
        public Object arg(int i, int j) {
          Checks.checkcond(i >= 0);
          Checks.checkcond(i < this.size());
          Checks.checkcond(j >= 0);
          Checks.checkcond(j < action(i).numParameterFactors());
          return tuple.get(paramName(fsmName, i, j));
        }

        @Override
        public boolean hasArg(int i, int j) {
          Checks.checkcond(i >= 0);
          Checks.checkcond(i < this.size());
          Checks.checkcond(j >= 0);
          Checks.checkcond(j < action(i).numParameterFactors());
          return tuple.containsKey(paramName(fsmName, i, j));
        }

        @Override
        public Args args(int i) {
          Checks.checkcond(i >= 0);
          Checks.checkcond(i < this.size());
          Object[] values = new Object[action(i).numParameterFactors()];
          for (int j = 0; j < values.length; j++) {
            values[j] = tuple.get(paramName(fsmName, i, j));
          }
          return new Args(values);
        }

        @Override
        public int size() {
          return historyLength;
        }
      };
    }
  }

  interface Observer {
    Observer SILENT = new Observer() {
      @Override
      public Observer createChild(String childName) {
        return this;
      }

      @Override
      public void startSequence(Story.Stage stage, ScenarioSequence seq) {
      }

      @Override
      public void run(Story.Stage stage, Scenario scenario, Object o) {
      }

      @Override
      public void passed(Story.Stage stage, Scenario scenario, Object o) {
      }

      @Override
      public void failed(Story.Stage stage, Scenario scenario, Object o, Expectation.Result result) {
      }

      @Override
      public void endSequence(Story.Stage stage, ScenarioSequence seq) {
      }
    };

    <SUT> void startSequence(Story.Stage stage, ScenarioSequence<SUT> seq);

    <SUT> void run(Story.Stage stage, Scenario<SUT> scenario, SUT sut);

    <SUT> void passed(Story.Stage stage, Scenario<SUT> scenario, SUT sut);

    <SUT> void failed(Story.Stage stage, Scenario<SUT> scenario, SUT sut, Expectation.Result result);

    <SUT> void endSequence(Story.Stage stage, ScenarioSequence<SUT> seq);

    Observer createChild(String childName);

    interface Factory {
      Observer createObserver(String fsmName);

      class ForSilent implements Factory {
        public static final Factory INSTANCE = new ForSilent();

        @Override
        public Observer createObserver(String fsmName) {
          return SILENT;
        }
      }

      class ForSimple implements Factory {
        public static final Factory INSTANCE = new ForSimple(System.out);
        private final PrintStream out;

        public ForSimple(PrintStream out) {
          this.out = Checks.checknotnull(out);
        }

        @Override
        public Observer createObserver(String fsmName) {
          return PrivateUtils.createSimpleObserver(fsmName, this.out);
        }
      }
    }
  }

  class PrivateUtils {
    private PrivateUtils() {
    }

    public static <SUT> String toString(ScenarioSequence<SUT> scenarioSequence) {
      Checks.checknotnull(scenarioSequence);
      Object[] scenarios = new Object[scenarioSequence.size()];
      for (int i = 0; i < scenarios.length; i++) {
        scenarios[i] = scenarioSequence.get(i);
      }
      return StringUtils.format("[%s]ScenarioSequence:[%s]", Thread.currentThread().getId(), StringUtils.join(",", scenarios));
    }

    static Observer createSimpleObserver(String fsmName, final PrintStream ps) {
      Checks.checknotnull(ps);
      return createSimpleObserver(fsmName, ps, 0);
    }

    private static Observer createSimpleObserver(final String fsmName, final PrintStream ps, final int generation) {
      Checks.checknotnull(ps);
      return new Observer() {
        private String indent(int level) {
          return new String(new char[2 * level]).replace("\0", " ");
        }

        @Override
        public Observer createChild(String childName) {
          return createSimpleObserver(childName, ps, generation + 1);
        }

        @Override
        public void startSequence(Story.Stage stage, ScenarioSequence scenarioSequence) {
          ps.println(StringUtils.format("%s[%s]Starting(%s#%s):%s", indent(generation), Thread.currentThread().getId(), fsmName, stage, scenarioSequence));
        }

        @Override
        public void run(Story.Stage stage, Scenario scenario, Object o) {
          ps.println(StringUtils.format("%s[%s]Running(%s#%s):%s expecting %s", indent(generation + 1), Thread.currentThread().getId(), fsmName, stage, scenario, scenario.then()));
        }

        @Override
        public void passed(Story.Stage stage, Scenario scenario, Object o) {
          ps.println(StringUtils.format("%s[%s]Passed(%s#%s)", indent(generation + 1), Thread.currentThread().getId(), fsmName, stage));
        }

        @Override
        public void failed(Story.Stage stage, Scenario scenario, Object o, Expectation.Result result) {
          ps.println(StringUtils.format("%s[%s]Failed(%s#%s): %s", indent(generation + 1), Thread.currentThread().getId(), fsmName, stage, result.getMessage()));
        }

        @Override
        public void endSequence(Story.Stage stage, ScenarioSequence seq) {
          ps.println(StringUtils.format("%s[%s]End(%s#%s)", indent(generation), Thread.currentThread().getId(), fsmName, stage));
        }
      };
    }
  }
}

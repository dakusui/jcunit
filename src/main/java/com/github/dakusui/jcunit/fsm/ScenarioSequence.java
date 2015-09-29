package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.io.PrintStream;
import java.io.Serializable;

/**
 * An interface that represents a sequence of scenarios.
 *
 * @param <SUT> A software (class) under test.
 */
public interface ScenarioSequence<SUT> extends Serializable {
  interface Observer {
    Observer SILENT = new Observer() {
      @Override
      public Observer createChild(String childName) {
        return this;
      }

      @Override
      public void startSequence(Type type, ScenarioSequence seq) {
      }

      @Override
      public void run(Type type, Scenario scenario, Object o) {
      }

      @Override
      public void passed(Type type, Scenario scenario, Object o) {
      }

      @Override
      public void failed(Type type, Scenario scenario, Object o, Expectation.Result result) {
      }

      @Override
      public void endSequence(Type type, ScenarioSequence seq) {
      }

      @Override
      public void skipSequence(Type type, ScenarioSequence seq) {
      }
    };

    <SUT> void startSequence(Type type, ScenarioSequence<SUT> seq);

    <SUT> void run(Type type, Scenario<SUT> scenario, SUT sut);

    <SUT> void passed(Type type, Scenario<SUT> scenario, SUT sut);

    <SUT> void failed(Type type, Scenario<SUT> scenario, SUT sut, Expectation.Result result);

    <SUT> void endSequence(Type type, ScenarioSequence<SUT> seq);

    <SUT> void skipSequence(Type type, ScenarioSequence<SUT> seq);

    Observer createChild(String childName);

    interface Factory {
      Observer createObserver(String fsmName);

      class ForSilent implements Factory {
        @Override
        public Observer createObserver(String fsmName) {
          return SILENT;
        }
      }

      class ForSimple implements Factory {
        @Override
        public Observer createObserver(String fsmName) {
          return Utils.createSimpleObserver(fsmName);
        }
      }
    }
  }

  class Utils {
    public static <SUT> String toString(ScenarioSequence<SUT> scenarioSequence) {
      Checks.checknotnull(scenarioSequence);
      Object[] scenarios = new Object[scenarioSequence.size()];
      for (int i = 0; i < scenarios.length; i++) {
        scenarios[i] = scenarioSequence.get(i);
      }
      return String.format("ScenarioSequence:[%s]", com.github.dakusui.jcunit.core.Utils.join(",", scenarios));
    }

    static Observer createSimpleObserver(String fsmName) {
      return createSimpleObserver(fsmName, System.out);
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
        public void startSequence(Type type, ScenarioSequence scenarioSequence) {
          ps.printf("%sStarting(%s#%s):%s\n", indent(generation), fsmName, type, scenarioSequence);
        }

        @Override
        public void run(Type type, Scenario scenario, Object o) {
          ps.printf("%sRunning(%s#%s):%s expecting %s\n", indent(generation + 1), fsmName, type, scenario, scenario.then());
        }

        @Override
        public void passed(Type type, Scenario scenario, Object o) {
          ps.printf("%sPassed(%s#%s)\n", indent(generation + 1), fsmName, type);
        }

        @Override
        public void failed(Type type, Scenario scenario, Object o, Expectation.Result result) {
          ps.printf("%sFailed(%s#%s): %s\n", indent(generation + 1), fsmName, type, result.getMessage());
        }

        @Override
        public void endSequence(Type type, ScenarioSequence seq) {
          ps.printf("%sEnd(%s#%s)\n", indent(generation), fsmName, type);
        }

        @Override
        public void skipSequence(Type type, ScenarioSequence seq) {
          ps.printf("%sSkip(%s#%s)\n", indent(generation), fsmName, type);
        }
      };
    }
  }

  abstract class Base<SUT> implements ScenarioSequence<SUT> {

    @Override
    public <T> void perform(T context, String name, Type type, SUT sut, Observer observer) {
      Checks.checknotnull(observer);
      observer.startSequence(type, this);
      try {
        for (int i = 0; i < this.size(); i++) {
          Scenario<SUT> each = this.get(i);
          Expectation.Result result = null;
          observer.run(type, each, sut);
          boolean passed = false;
          try {
            ////
            // Only for the first scenario, make sure SUT is in the 'given' state.
            // We'll see broken test results later on in case it doesn't meet the
            // precondition described as the state, otherwise.
            if (i == 0) {
              if (!each.given.check(sut)) {
                throw new Expectation.Result.Builder("Precondition was not satisfied.").addFailedReason(String.format("SUT(%s) isn't in state '%s'", sut, each.given)).build();
              }
            }
            Object r = each.perform(context, sut);
            passed = true;
            ////
            // each.perform(sut) didn't throw an exception
            //noinspection unchecked,ThrowableResultOfMethodCallIgnored
            result = each.then().checkReturnedValue(context, sut, r, observer);
          } catch (Expectation.Result r) {
            result = r;
          } catch (Throwable t) {
            if (!passed) {
              //noinspection unchecked,ThrowableResultOfMethodCallIgnored
              result = each.then().checkThrownException(context, sut, t, observer);
            } else {
              throw new RuntimeException("Expectation#checkReturnedValue threw an exception. This is considered to be a framework side's bug.", t);
            }
          } finally {
            if (result != null) {
              if (result.isSuccessful())
                observer.passed(type, each, sut);
              else
                observer.failed(type, each, sut, result);
              result.throwIfFailed();
            }
          }
        }
      } finally {
        observer.endSequence(type, this);
      }
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

    @Override public int hashCode() {
      return this.toString().hashCode();
    }

    @Override public boolean equals(Object another) {
      if (another instanceof ScenarioSequence) {
        ScenarioSequence anotherSequence = (ScenarioSequence) another;
        return Utils.toString(this).equals(Utils.toString(anotherSequence));
      }
      return false;
    }


    @Override
    public String toString() {
      return Utils.toString(this);
    }
  }

  ScenarioSequence<?> EMPTY = new ScenarioSequence() {
    @Override
    public void perform(Object context, String name, Type type, Object sut, Observer observer) {
      // Does nothing since this is an emptry scenario object.
      observer.skipSequence(type, this);
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public Scenario<?> get(int i) {
      throw new IllegalStateException();
    }

    @Override
    public State<?> state(int i) {
      throw new IllegalStateException();
    }

    @Override
    public Action<?> action(int i) {
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
      return "Empty Story:[]";
    }
  };

  /**
   * Performs this scenario with given {@code sut} object.
   *
   * @param sut An objects that represents software under test.
   */
  <T> void perform(T context, String name, Type type, SUT sut, Observer observer);

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

  enum Type {
    setUp,
    main,
    optional
  }

  /**
   * Builds a {@code Story} object from a {@code Tuple} using  a given {@code FSMFactorbs}.
   *
   * @param <SUT> A class of software under test.
   */
  class BuilderFromTuple<SUT> {
    private FSMFactors factors;
    private Tuple      tuple;
    private String     fsmName;

    public BuilderFromTuple() {
    }

    public BuilderFromTuple<SUT> setFSMFactors(FSMFactors factors) {
      this.factors = factors;
      return this;
    }

    public BuilderFromTuple<SUT> setTuple(Tuple tuple) {
      this.tuple = tuple;
      return this;
    }

    public BuilderFromTuple<SUT> setFSMName(String fsmName) {
      this.fsmName = fsmName;
      return this;
    }

    public ScenarioSequence<SUT> build() {
      Checks.checknotnull(tuple);
      Checks.checknotnull(factors);
      Checks.checknotnull(fsmName);
      Checks.checkcond(factors.historyLength(fsmName) > 0);
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
          return (State<SUT>) tuple.get(factors.stateFactorName(fsmName, i));
        }

        @Override
        public Action<SUT> action(int i) {
          Checks.checkcond(i >= 0);
          Checks.checkcond(i < this.size());
          //noinspection unchecked
          return (Action<SUT>) tuple.get(factors.actionFactorName(fsmName, i));
        }

        @Override
        public Object arg(int i, int j) {
          Checks.checkcond(i >= 0);
          Checks.checkcond(i < this.size());
          Checks.checkcond(j >= 0);
          Checks.checkcond(j < action(i).numParameterFactors());
          return tuple.get(factors.paramFactorName(fsmName, i, j));
        }

        @Override
        public boolean hasArg(int i, int j) {
          Checks.checkcond(i >= 0);
          Checks.checkcond(i < this.size());
          Checks.checkcond(j >= 0);
          Checks.checkcond(j < action(i).numParameterFactors());
          return tuple.containsKey(factors.paramFactorName(fsmName, i, j));
        }

        @Override
        public Args args(int i) {
          Checks.checkcond(i >= 0);
          Checks.checkcond(i < this.size());
          Object[] values = new Object[action(i).numParameterFactors()];
          for (int j = 0; j < values.length; j++) {
            values[j] = tuple.get(factors.paramFactorName(fsmName, i, j));
          }
          return new Args(values);
        }

        @Override
        public int size() {
          return factors.historyLength(fsmName);
        }

        @Override
        public String toString() {
          return Utils.toString(this);
        }
      };
    }
  }
}

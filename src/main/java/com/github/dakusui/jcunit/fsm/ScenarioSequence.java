package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.ArrayList;
import java.util.List;

public interface ScenarioSequence<SUT> {
  public static interface Reporter<SUT> {
    void startSequence(ScenarioSequence<SUT> seq);

    void run(Scenario<SUT> scenario, SUT sut);

    void passed(Scenario<SUT> scenario, SUT sut);

    void failed(Scenario<SUT> scenario, SUT sut);

    void endSequence(ScenarioSequence<SUT> seq);
  }

  public static final Reporter SILENT_REPORTER = new Reporter() {
    @Override
    public void startSequence(ScenarioSequence seq) {
    }

    @Override
    public void run(Scenario scenario, Object o) {
    }

    @Override
    public void passed(Scenario scenario, Object o) {
    }

    @Override
    public void failed(Scenario scenario, Object o) {
    }

    @Override
    public void endSequence(ScenarioSequence seq) {

    }
  };

  public static final Reporter SIMPLE_REPORTER = new Reporter() {
    @Override
    public void startSequence(ScenarioSequence seq) {
      System.out.printf("Starting:%s\n", seq);
    }

    @Override
    public void run(Scenario scenario, Object o) {
      System.out.printf("  Running:%s\n", scenario);
    }

    @Override
    public void passed(Scenario scenario, Object o) {
      System.out.println("  Passed");
    }

    @Override
    public void failed(Scenario scenario, Object o) {
      System.out.println("  Failed");
    }

    @Override
    public void endSequence(ScenarioSequence seq) {
      System.out.println("End");
    }
  };

  public static final ScenarioSequence<?> EMPTY = new ScenarioSequence() {
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
      return "ScenarioSequence:[]";
    }
  };


  int size();

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

  Object arg(int i, int j);

  boolean hasArg(int i, int j);

  Args args(int i);

  /**
   * Builds a {@code ScenarioSequence} object from a {@code Tuple} using  a given {@code FSMFactorbs}.
   *
   * @param <SUT> A class of software under test.
   */
  public static class BuilderFromTuple<SUT> {
    private FSMFactors factors;
    private Tuple      tuple;

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

    public ScenarioSequence<SUT> build() {
      Checks.checknotnull(tuple);
      Checks.checknotnull(factors);
      Checks.checkcond(factors.historyLength() > 0);
      return new ScenarioSequence<SUT>() {
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
          return (State<SUT>) tuple.get(factors.stateFactorName(i));
        }

        @Override
        public Action<SUT> action(int i) {
          Checks.checkcond(i >= 0);
          Checks.checkcond(i < this.size());
          //noinspection unchecked
          return (Action<SUT>) tuple.get(factors.actionFactorName(i));
        }

        @Override
        public Object arg(int i, int j) {
          Checks.checkcond(i >= 0);
          Checks.checkcond(i < this.size());
          Checks.checkcond(j >= 0);
          Checks.checkcond(j < action(i).numParameterFactors());
          return tuple.get(factors.paramFactorName(i, j));
        }

        @Override
        public boolean hasArg(int i, int j) {
          Checks.checkcond(i >= 0);
          Checks.checkcond(i < this.size());
          Checks.checkcond(j >= 0);
          Checks.checkcond(j < action(i).numParameterFactors());
          return tuple.containsKey(factors.paramFactorName(i, j));
        }

        @Override
        public Args args(int i) {
          Checks.checkcond(i >= 0);
          Checks.checkcond(i < this.size());
          Object[] values = new Object[action(i).numParameterFactors()];
          for (int j = 0; j < values.length; j++) {
            values[j] = tuple.get(factors.paramFactorName(i, j));
          }
          return new Args(values);
        }

        @Override
        public int size() {
          return factors.historyLength();
        }

        @Override
        public String toString() {
          return FSMUtils.toString(this);
        }
      };
    }
  }
}

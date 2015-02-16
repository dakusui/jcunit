package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.tuples.Tuple;

public interface ScenarioSequence<SUT> {

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
      return "Story:[]";
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
   * Builds a {@code Story} object from a {@code Tuple} using  a given {@code FSMFactorbs}.
   *
   * @param <SUT> A class of software under test.
   */
  public static class BuilderFromTuple<SUT> {
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
          return FSMUtils.toString(this);
        }
      };
    }
  }
}

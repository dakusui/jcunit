package com.github.dakusui.jcunit.coverage;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.fsm.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FSMMetrics extends Metrics.Base implements Metrics {
  private final FSM<?> targetFSM;
  private final int    historyLength;
  private final String targetFSMName;
  private final int    switchCoverage;

  public FSMMetrics(String fsmName, FSM<?> fsm, int historyLength, int switchCoverage) {
    Checks.checknotnull(fsmName);
    Checks.checknotnull(fsm);
    Checks.checkcond(historyLength > 0);
    this.targetFSMName = fsmName;
    this.targetFSM = fsm;
    this.historyLength = historyLength;
    this.switchCoverage = 1;
  }

  private ScenarioSequence createScenarioSequenceFromTuple(Tuple tuple) {
    return new ScenarioSequence.BuilderFromTuple()
        .setTuple(tuple)
        .setFSMName(this.targetFSMName)
        .setHistoryLength(FSMMetrics.this.historyLength)
        .build();
  }

  @Metrics.Base.MetricItem
  public Metric.CoverageMetric stateCoverage() {
    return new Metric.CoverageMetric<State>(new HashSet<State>(targetFSM.states())) {
      @Override
      protected Set<State> getCoveredItemsBy(Tuple tuple) {
        ScenarioSequence scenarioSequence = createScenarioSequenceFromTuple(tuple);
        Set<State> ret = new HashSet<State>();
        for (int i = 0; i < scenarioSequence.size(); i++) {
          ret.add(scenarioSequence.get(i).given);
        }
        return ret;
      }

      @Override
      public String name() {
        return "State coverage";
      }
    };
  }

  @Metrics.Base.MetricItem
  public Metric.CoverageMetric<Action> actionCoverage() {
    return new Metric.CoverageMetric<Action>(new HashSet<Action>(targetFSM.actions())) {
      @Override
      protected Set<Action> getCoveredItemsBy(Tuple tuple) {
        ScenarioSequence scenarioSequence = createScenarioSequenceFromTuple(tuple);
        Set<Action> ret = new HashSet<Action>();
        for (int i = 0; i < scenarioSequence.size(); i++) {
          ret.add(scenarioSequence.get(i).when);
        }
        return ret;
      }

      @Override
      public String name() {
        return "Action coverage";
      }
    };
  }

  @Metrics.Base.MetricItem
  public Metric.CoverageMetric<Switch> switchCoverage() {
    final int degree = 1;
    Checks.checkcond(this.historyLength > degree);
    return new Metric.CoverageMetric<Switch>(allSwithesOf(this.targetFSM, this.switchCoverage)) {
      @Override
      protected Set<Switch> getCoveredItemsBy(Tuple tuple) {
        ScenarioSequence scenarioSequence = createScenarioSequenceFromTuple(tuple);
        Set<Switch> ret = new HashSet<Switch>();
        for (int i = 0; i < scenarioSequence.size() - degree; i++) {
          for (int j = 0; j < degree; j++) {
            ret.add(new Switch(degree, createScenarioSequence(scenarioSequence, j, degree + 1)));
          }
        }
        return ret;
      }

      @Override
      public String name() {
        return "Switch coverage (1)";
      }
    };
  }

  private static Set<Switch> allSwithesOf(FSM<?> targetFSM, int degree) {
    Checks.checknotnull(targetFSM);
    Checks.checkcond(degree >= 1);
    Set<Switch> ret = new HashSet<Switch>();
    if (degree == 1) {
      for (State eachState : targetFSM.states()) {
        for (Action from : actionsFrom(eachState, targetFSM)) {
          for (Action to : actionsTo(eachState, targetFSM)) {
            ret.add(new Switch(from, eachState, to));
          }
        }
      }
    } else {
      for (Switch each : allSwithesOf(targetFSM, degree - 1)) {
        for (State nextState : nextStatesOf(each, targetFSM)) {
          for (Action nextAction : actionsFrom(nextState, targetFSM)) {
            ret.add(new Switch(each, nextState, nextAction));
          }
        }
      }
    }
    return ret;
  }

  private static Set<Action> actionsFrom(State state, FSM targetFSM) {
    return new HashSet<Action>();
  }

  private static Set<Action> actionsTo(State state, FSM targetFSM) {
    return new HashSet<Action>();
  }

  private static State[] nextStatesOf(Switch sw, FSM targetFSM) {
    return new State[0];
  }

  private ScenarioSequence createScenarioSequence(final ScenarioSequence sequence, final int offset, final int length) {
    Checks.checknotnull(sequence);
    Checks.checkcond(sequence.size() >= offset + length);
    return new ScenarioSequence() {
      @Override
      public int size() {
        return length;
      }

      @Override
      public Scenario get(int i) {
        return sequence.get(i + offset);
      }

      @Override
      public State state(int i) {
        return sequence.state(i + offset);
      }

      @Override
      public Action action(int i) {
        return sequence.action(i + offset);
      }

      @Override
      public Object arg(int i, int j) {
        return sequence.arg(i + offset, j);
      }

      @Override
      public boolean hasArg(int i, int j) {
        return sequence.hasArg(i + offset, j);
      }

      @Override
      public Args args(int i) {
        return sequence.args(i + offset);
      }

      @Override
      public void perform(Story.Context context, FSMUtils.Synchronizer synchronizer, FSMUtils.Synchronizable token, Observer observer) {
        throw new UnsupportedOperationException("This object isn't meant to be performed.");
      }
    };
  }

  public static class Switch {
    private final List<State>  states;
    private final List<Action> actions;

    private Switch(int degree) {
      this.states = new ArrayList<State>(degree);
      this.actions = new ArrayList<Action>(degree + 1);
    }
    Switch(Action in, State state, Action out) {
      this(1);
      this.actions.add(in);
      this.states.add(state);
      this.actions.add(out);
    }
    Switch(int degree, ScenarioSequence<?> sequence) {
      this(degree);
      for (int i = 0; i <= degree; i++) {
        if (i > 0) {
          states.add(sequence.get(i).given);
        }
        actions.add(sequence.get(i).when);
      }
    }

    public Switch(Switch base, State nextState, Action nextAction) {
      this(base.states.size() + 1);
      Checks.checknotnull(nextAction);
      Checks.checknotnull(nextState);
      Checks.checknotnull(base);
      this.states.addAll(base.states);
      this.states.add(nextState);
      this.actions.addAll(base.actions);
      this.actions.add(nextAction);
    }

    @Override
    public int hashCode() {
      return this.states.hashCode() + this.actions.hashCode();
    }

    @Override
    final public boolean equals(Object anotherObject) {
      if (!(anotherObject instanceof Switch))
        return false;
      Switch another = (Switch) anotherObject;
      return this.states.equals(another.states) && this.actions.equals(another.actions);
    }
  }
}

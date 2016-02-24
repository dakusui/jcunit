package com.github.dakusui.jcunit.coverage;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.fsm.Action;
import com.github.dakusui.jcunit.fsm.FSM;
import com.github.dakusui.jcunit.fsm.ScenarioSequence;
import com.github.dakusui.jcunit.fsm.State;

import java.util.HashSet;
import java.util.Set;

public class FSMMetrics extends Metrics.Base implements Metrics {
  private final FSM<?> targetFSM;
  private final int historyLength;
  private final String targetFSMName;

  public FSMMetrics(String fsmName, FSM<?> fsm, int historyLength) {
    Checks.checknotnull(fsmName);
    Checks.checknotnull(fsm);
    Checks.checkcond(historyLength > 0);
    this.targetFSMName = fsmName;
    this.targetFSM = fsm;
    this.historyLength = historyLength;
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
  public Metric.CoverageMetric actionCoverage() {
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
}

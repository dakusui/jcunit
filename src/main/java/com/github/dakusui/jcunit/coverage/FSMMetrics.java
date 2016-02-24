package com.github.dakusui.jcunit.coverage;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.fsm.FSM;
import com.github.dakusui.jcunit.fsm.ScenarioSequence;
import com.github.dakusui.jcunit.fsm.State;

import java.util.HashSet;
import java.util.Set;

public class FSMMetrics extends Metrics.Base implements Metrics {
  private final FSM<?> targetFSM;
  private final int historyLength;

  public FSMMetrics(FSM<?> fsm, int historyLength) {
    Checks.checknotnull(fsm);
    Checks.checkcond(historyLength > 0);
    this.targetFSM = fsm;
    this.historyLength = historyLength;
  }

  @Metrics.Base.MetricItem
  public Metric.CoverageMetric stateCoverage() {
    return new Metric.CoverageMetric<State>(new HashSet<State>(targetFSM.states())) {
      @Override
      protected Set<State> getCoveredItemsBy(Tuple tuple) {
        ScenarioSequence scenarioSequence = new ScenarioSequence.BuilderFromTuple()
            .setTuple(tuple)
            .setFSMName(this.name())
            .setHistoryLength(FSMMetrics.this.historyLength)
            .build();
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
}

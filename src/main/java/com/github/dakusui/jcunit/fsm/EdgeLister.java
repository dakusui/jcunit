package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;

import java.util.LinkedList;
import java.util.List;

public interface EdgeLister<SUT> {
  List<FSM.Edge<SUT>> possibleEdgesFrom(State<SUT> state);

  class Base<SUT> implements EdgeLister<SUT> {
    private final List<ScenarioSequence<SUT>> mainScenarioSequences;

    public Base(List<ScenarioSequence<SUT>> scenarioSequences) {
      this.mainScenarioSequences = Checks.checknotnull(scenarioSequences);
    }

    @Override
    public List<FSM.Edge<SUT>> possibleEdgesFrom(State<SUT> state) {
      List<FSM.Edge<SUT>> ret = new LinkedList<FSM.Edge<SUT>>();
      for (ScenarioSequence<SUT> eachScenario : this.mainScenarioSequences) {
        for (int i = 0; i < eachScenario.size(); i++) {
          Scenario<SUT> each = eachScenario.get(i);
          if (each.given.equals(state) && !each.then().state
              .equals(State.Void.getInstance())) {
            FSM.Edge<SUT> t = new FSM.Edge<SUT>(eachScenario.action(i),
                eachScenario.args(i));
            if (!ret.contains(t))
              ret.add(t);
          }
        }
      }
      return ret;
    }
  }
}

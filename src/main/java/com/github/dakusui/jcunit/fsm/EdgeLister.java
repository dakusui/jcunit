package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;

import java.util.LinkedList;
import java.util.List;

public class EdgeLister {
  private final List<ScenarioSequence> mainScenarioSequences;

  public EdgeLister(List<ScenarioSequence> scenarioSequences) {
    this.mainScenarioSequences = Checks.checknotnull(scenarioSequences);
  }

  protected List<StateRouter.Edge> possibleEdgesFrom(State state) {
    List<StateRouter.Edge> ret = new LinkedList<StateRouter.Edge>();
    for (ScenarioSequence<?> eachScenario : this.mainScenarioSequences) {
      for (int i = 0; i < eachScenario.size(); i++) {
        Scenario each = eachScenario.get(i);
        if (each.given.equals(state) && !each.then().state
            .equals(State.Void.getInstance())) {
          //noinspection unchecked
          StateRouter.Edge<?> t = new StateRouter.Edge(eachScenario.action(i),
              eachScenario.args(i));
          if (!ret.contains(t))
            ret.add(t);
        }
      }
    }
    return ret;
  }
}

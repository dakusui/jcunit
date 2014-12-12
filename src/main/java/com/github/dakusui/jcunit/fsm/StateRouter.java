package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class StateRouter<SUT> {
  public static class Transition<SUT> {
    public final Action<SUT> action;
    public final Args        args;

    public Transition(Action<SUT> action, Args args) {
      this.action = action;
      this.args = args;
    }
  }

  private final List<State> destinations;
  private final Map<State<SUT>, ScenarioSequence<SUT>> routes;

  public StateRouter(FSM<SUT> fsm, List<State> destinations) {
    Checks.checknotnull(fsm);
    Checks.checknotnull(destinations);
    this.destinations = Collections.unmodifiableList(Utils.singleton(destinations));
    this.routes = new HashMap<State<SUT>, ScenarioSequence<SUT>>();
    for (State<SUT> each : destinations) {
      if (each.equals(fsm.initialState())) {
        this.routes.put(each, (ScenarioSequence<SUT>) ScenarioSequence.EMPTY);
      } else {
        this.routes.put(each, null);
      }
    }
  }

  protected abstract List<Transition> possibleTransitionsFrom(State<SUT> state);

  private static <SUT> void route(Map<State<SUT>, ScenarioSequence<SUT>> routes, List<Transition> current) {

  }
}

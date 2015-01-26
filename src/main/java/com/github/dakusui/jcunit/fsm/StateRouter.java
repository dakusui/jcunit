package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;

import java.util.*;

public abstract class StateRouter<SUT> {

  public static class Transition<SUT> {
    public final Action<SUT> action;
    public final Args        args;

    public Transition(Action<SUT> action, Args args) {
      this.action = action;
      this.args = args;
    }
  }

  private final FSM<SUT> fsm;
  private final List<State<SUT>> destinations;
  private final Map<State<SUT>, ScenarioSequence<SUT>> routes;

  public StateRouter(FSM<SUT> fsm, List<State<SUT>> destinations) {
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
    this.fsm = fsm;
    traverse(fsm.initialState(), new LinkedList<Transition>(), new HashSet<State<SUT>>());
    List<State<SUT>> unreachableDestinations = new ArrayList<State<SUT>>(this.destinations.size());
    for (State<SUT> each : this.destinations) {
      if (this.routes.get(each) == null) {
        unreachableDestinations.add(each);
      }
    }
    Checks.checktest(
            unreachableDestinations.size() == 0,
            "The states '%s' can't be reached from the initial state of the given FSM.",
            unreachableDestinations,
            this.fsm.initialState()
    );
  }

  public boolean isDestination(State<SUT> state) {
    return this.destinations.contains(state);
  }

  public ScenarioSequence<SUT> routeTo(State<SUT> state) {
    Checks.checkcond(this.destinations.contains(state));
    return this.routes.get(state);
  }

  private void traverse(State<SUT> state, List<Transition> path, Set<State<SUT>> visited) {
    for (Transition each : possibleTransitionsFrom(state)) {
      State<SUT> next = next(state, each);
      if (next == State.VOID) return;
      if (visited.contains(visited))
        continue;
      visited.add(next);
      List<Transition> pathToNext = new LinkedList<Transition>(path);
      pathToNext.add(each);

      if (this.destinations.contains(next)) {
        this.routes.put(next, buildScenarioSequenceFromTransitions(pathToNext));
      }
      traverse(next, pathToNext, visited);
    }
  }

  private ScenarioSequence<SUT> buildScenarioSequenceFromTransitions(final List<Transition> pathToNext) {
    return new ScenarioSequence<SUT>() {
      @Override
      public int size() {
        return pathToNext.size();
      }

      @Override
      public Scenario<SUT> get(int i) {
        return new Scenario<SUT>(state(i), action(i), args(i));
      }

      @Override
      public State<SUT> state(int i) {
        Checks.checkcond(i >= 0 && i < size());
        State<SUT> ret = StateRouter.this.fsm.initialState();
        for (int c = 0; c < i; c++) {
          next(ret, new Transition<SUT>(action(i), args(i)));
        }
        return ret;
      }

      @Override
      public Action<SUT> action(int i) {
        return pathToNext.get(i).action;
      }

      @Override
      public Object arg(int i, int j) {
        return this.args(i).values()[j];
      }

      @Override
      public boolean hasArg(int i, int j) {
        Checks.checkcond(j >= 0);
        return args(i).size() > j;
      }

      @Override
      public Args args(int i) {
        return pathToNext.get(i).args;
      }
    };
  }

  private State<SUT> next(State<SUT> state, Transition<SUT> t) {
    return state.expectation(t.action, t.args).state;
  }

  protected abstract List<Transition> possibleTransitionsFrom(State<SUT> state);


}

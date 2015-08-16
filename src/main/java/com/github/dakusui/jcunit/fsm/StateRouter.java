package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;

import java.util.*;

public abstract class StateRouter<SUT> {

  private final FSM<SUT>                               fsm;
  private final List<State<SUT>>                       destinations;
  private final Map<State<SUT>, ScenarioSequence<SUT>> routes;

  public StateRouter(FSM<SUT> fsm, List<State<SUT>> destinations) {
    Checks.checknotnull(fsm);
    Checks.checknotnull(destinations);
    this.destinations = Collections.unmodifiableList(Utils.singleton(destinations));
    this.routes = new LinkedHashMap<State<SUT>, ScenarioSequence<SUT>>();
    for (State<SUT> each : destinations) {
      if (each.equals(fsm.initialState())) {
        //noinspection unchecked
        this.routes.put(each, (ScenarioSequence<SUT>) ScenarioSequence.EMPTY);
      } else {
        this.routes.put(each, null);
      }
    }
    this.fsm = fsm;
    traverse(fsm.initialState(), new LinkedList<Transition<SUT>>(), new LinkedHashSet<State<SUT>>());
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

  public ScenarioSequence<SUT> routeTo(State<SUT> state) {
    Checks.checkcond(this.destinations.contains(state));
    return this.routes.get(state);
  }

  private void traverse(State<SUT> state, List<Transition<SUT>> path, Set<State<SUT>> visited) {
    for (Transition<SUT> each : possibleTransitionsFrom(state)) {
      State<SUT> next = next(state, each);
      if (next == State.VOID)
        return;
      if (visited.contains(next))
        continue;
      visited.add(next);
      List<Transition<SUT>> pathToNext = new LinkedList<Transition<SUT>>(path);
      pathToNext.add(each);

      if (this.destinations.contains(next)) {
        this.routes.put(next, buildStoryFromTransitions(pathToNext));
      }
      traverse(next, pathToNext, visited);
    }
  }

  private ScenarioSequence<SUT> buildStoryFromTransitions(final List<Transition<SUT>> pathToNext) {
    return new ScenarioSequence.Base<SUT>() {
      @Override
      public int size() {
        return pathToNext.size();
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

      @Override
      public String toString() {
        return FSMUtils.toString(this);
      }
    };
  }

  private State<SUT> next(State<SUT> state, Transition<SUT> t) {
    return state.expectation(t.action, t.args).state;
  }

  protected abstract List<Transition<SUT>> possibleTransitionsFrom(State<SUT> state);

  public static class Transition<SUT> {
    public final Action<SUT> action;
    public final Args        args;

    public Transition(Action<SUT> action, Args args) {
      this.action = action;
      this.args = args;
    }

    @Override
    public int hashCode() {
      return this.action.hashCode();
    }

    @Override
    public boolean equals(Object anotherObject) {
      if (!(anotherObject instanceof Transition))
        return false;
      //noinspection unchecked
      Transition<SUT> another = (Transition<SUT>) anotherObject;
      return this.action.equals(another.action) && Arrays.deepEquals(this.args.values(), another.args.values());
    }
  }


}

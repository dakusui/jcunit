package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.FactorSpace;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArray;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.caengines.IPO2CoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.caengines.SimpleCoveringArrayEngine;

import java.util.*;

import static com.github.dakusui.jcunit.core.Checks.checkcond;
import static com.github.dakusui.jcunit.core.Checks.checknotnull;
import static com.github.dakusui.jcunit.core.Checks.checktest;

public interface StateRouter<SUT> {
  ScenarioSequence<SUT> routeTo(State<SUT> state);

  class Base<SUT> implements StateRouter<SUT> {
    private final Map<State<SUT>, List<State<SUT>>>  adjacents;
    private final Map<StatePair<SUT>, FSM.Edge<SUT>> links;
    private final State<SUT>                         initialState;


    public Base(FSM<SUT> fsm) {
      this.adjacents = buildAdjacents(fsm);
      this.links = buildLinks(fsm);
      this.initialState = fsm.initialState();
    }

    @Override
    public ScenarioSequence<SUT> routeTo(State<SUT> state) {
      if (this.initialState.equals(state)) return ScenarioSequence.Empty.getInstance();
      List<FSM.Edge<SUT>> route = Utils.newList();
      checktest(
          route(this.initialState, state, route, Utils.<State<SUT>>newSet()),
          "The state '%s' can't be reached from the initial state of the given FSM.",
          state,
          this.initialState
      );
      return buildScenarioSequenceFromTransitions(route);
    }

    private ScenarioSequence<SUT> buildScenarioSequenceFromTransitions(final List<FSM.Edge<SUT>> route) {
      return new ScenarioSequence.Base<SUT>() {
        @Override
        public int size() {
          return route.size();
        }

        @Override
        public State<SUT> state(int i) {
          checkcond(i >= 0 && i < size());
          State<SUT> ret = StateRouter.Base.this.initialState;
          for (int c = 0; c < i; c++) {
            ret = next(ret, new FSM.Edge<SUT>(action(c), args(c)));
          }
          return ret;
        }

        @Override
        public Action<SUT> action(int i) {
          return route.get(i).action;
        }

        @Override
        public Object arg(int i, int j) {
          return this.args(i).values()[j];
        }

        @Override
        public boolean hasArg(int i, int j) {
          checkcond(j >= 0);
          return args(i).size() > j;
        }

        @Override
        public Args args(int i) {
          return route.get(i).args;
        }

        @Override
        public String toString() {
          return PrivateUtils.toString(this);
        }

      };
    }

    boolean route(State<SUT> cur, State<SUT> to, List<FSM.Edge<SUT>> route, Set<State<SUT>> visited) {
      if (!this.adjacents.containsKey(cur)) return false;
      for (State<SUT> each : checknotnull(this.adjacents.get(cur))) {
        route.add(this.links.get(new StatePair<SUT>(cur, each)));
        if (each.equals(to)) {
          return true;
        } else {
          if (!visited.contains(each)) {
            visited.add(each);
            if (route(each, to, route, visited)) {
              return true;
            }
          }
        }
        if (!route.isEmpty()) {
          route.remove(route.size() - 1);
        }
      }
      return false;
    }

    Map<State<SUT>, List<State<SUT>>> buildAdjacents(FSM<SUT> fsm) {
      // from -> tos
      final Map<State<SUT>, List<State<SUT>>> ret = Utils.newMap();
      for (State<SUT> eachFromState : fsm.states()) {
        for (Action<SUT> eachAction : fsm.actions()) {
          for (Args eachArgs : possibleArgsList(eachAction)) {
            State<SUT> eachToState = eachFromState.expectation(eachAction, eachArgs).state;
            if (State.Void.getInstance().equals(eachToState))
              continue;
            List<State<SUT>> toStates = getToStates(ret, eachFromState);
            if (!toStates.contains(eachToState))
              toStates.add(eachToState);
          }
        }
      }
      return ret;
    }

    private List<State<SUT>> getToStates(Map<State<SUT>, List<State<SUT>>> ret, State<SUT> eachFromState) {
      if (!ret.containsKey(eachFromState))
        ret.put(eachFromState, Utils.<State<SUT>>newList());
      return ret.get(eachFromState);
    }

    Map<StatePair<SUT>, FSM.Edge<SUT>> buildLinks(FSM<SUT> fsm) {
      final Map<StatePair<SUT>, FSM.Edge<SUT>> edges = Utils.newMap();
      for (State<SUT> fromState : fsm.states()) {
        for (Action<SUT> eachAction : fsm.actions()) {
          for (Args eachArgs : possibleArgsList(eachAction)) {
            State<SUT> toState = fromState.expectation(eachAction, eachArgs).state;
            if (State.Void.getInstance().equals(toState))
              continue;
            StatePair<SUT> link = new StatePair<SUT>(fromState, toState);
            if (edges.containsKey(link))
              continue;
            edges.put(link, new FSM.Edge<SUT>(eachAction, eachArgs));
          }
        }
      }
      return edges;
    }


    List<Args> possibleArgsList(final Action<SUT> action) {
      // TODO:  make it cleaner a bit
      if (action.parameters().size() == 0) {
        return Collections.singletonList(new Args(new Object[0]));
      }
      final CoveringArrayEngine tg;
      if (action.parameters().size() == 1) {
        tg = new SimpleCoveringArrayEngine();
      } else {
        tg =  new IPO2CoveringArrayEngine(2);
      }
      final FactorSpace factorSpace = new FactorSpace(action.parameters(), action.parameters().getConstraintChecker());
      final CoveringArray coveringArray = tg.generate(factorSpace);
      return new AbstractList<Args>() {
        @Override
        public Args get(int index) {
          return new Utils.Form<Tuple, Args>() {
            @Override
            public Args apply(final Tuple inTuple) {
              List<Object> tmp = Utils.transform(action.parameters(), new Utils.Form<Factor, Object>() {
                @Override
                public Object apply(Factor inFactor) {
                  return inTuple.get(inFactor.name);
                }
              });
              return new Args(tmp.toArray());
            }
          }.apply(coveringArray.get(index));
        }

        @Override
        public int size() {
          return coveringArray.size();
        }
      };
    }

    static class StatePair<SUT> {
      State<SUT> from;
      State<SUT> to;

      StatePair(State<SUT> from, State<SUT> to) {
        this.from = checknotnull(from);
        this.to = checknotnull(to);
      }

      public int hashCode() {
        return this.from.hashCode();
      }

      public boolean equals(Object anotherObject) {
        if (!(anotherObject instanceof StatePair))
          return false;
        StatePair another = (StatePair) anotherObject;
        return this.from.equals(another.from) && this.to.equals(another.to);
      }
    }

    private static <SUT> State<SUT> next(State<SUT> state, FSM.Edge<SUT> t) {
      return state.expectation(t.action, t.args).state;
    }
  }
}

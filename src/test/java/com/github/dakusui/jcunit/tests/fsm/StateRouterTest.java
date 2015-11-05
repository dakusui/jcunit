package com.github.dakusui.jcunit.tests.fsm;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.ParametersSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;
import com.github.dakusui.jcunit.plugins.generators.IPO2TupleGenerator;
import com.github.dakusui.jcunit.plugins.generators.TupleGenerator;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import org.junit.Test;

import java.util.AbstractList;
import java.util.List;
import java.util.Map;

import static com.github.dakusui.jcunit.core.Checks.checknotnull;

//@RunWith(JCUnit.class)
public class StateRouterTest {
  public static class Example {
    public void action(int p1, int p2) {

    }
  }

  public enum Spec implements FSMSpec<Example> {
    @StateSpec I {
      @Override
      public Expectation<Example> action(Expectation.Builder<Example> b, int p1, int p2) {
        if (p1 > 0)
          return b.valid(S0).build();
        return b.invalid().build();
      }
    },
    @StateSpec S0 {
      @Override
      public Expectation<Example> action(Expectation.Builder<Example> b, int p1, int p2) {
        if (p1 > 1)
          return b.valid(S1).build();
        return b.invalid().build();
      }
    },
    @StateSpec S1 {
      @Override
      public Expectation<Example> action(Expectation.Builder<Example> b, int p1, int p2) {
        return b.invalid().build();
      }
    };

    @ParametersSpec
    public final static Parameters action = new Parameters.Builder()
        .add("p1", 0, 1, 2)
        .add("p2", 0, 1, 2)
        .build();

    @ActionSpec
    public abstract Expectation<Example> action(Expectation.Builder<Example> b, int p1, int p2);

    @Override
    public boolean check(Example example) {
      return true;
    }
  }

  FSM<Example> createFSM() {
    return new FSM.Base<Example>("example", Spec.class, 3);
  }


  @Test
  public void test() {
    class StatePair<SUT> {
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

    final FSM<Example> fsm = createFSM();
    final Map<StatePair<Example>, StateRouter.Edge<Example>> edges = Utils.newMap();
    for (State<Example> fromState : fsm.states()) {
      for (Action<Example> eachAction : fsm.actions()) {
        for (Args eachArgs : possibleArgsList(eachAction)) {
          State<Example> toState = fromState.expectation(eachAction, eachArgs).state;
          if (State.Void.getInstance().equals(toState))
            continue;
          StatePair<Example> link = new StatePair<Example>(fromState, toState);
          if (edges.containsKey(link))
            continue;
          edges.put(link, new StateRouter.Edge<Example>(eachAction, eachArgs));
        }
      }
    }

    StateRouter<Example> router = new StateRouter<Example>(
        fsm,
        new EdgeLister<Example>() {
          @Override
          public List<StateRouter.Edge<Example>> possibleEdgesFrom(State<Example> state) {
            List<StateRouter.Edge<Example>> ret = Utils.newList();
            for (StatePair<Example> each : edges.keySet()) {
              if (each.from.equals(state)) {
                ret.add(edges.get(each));
              }
            }
            return ret;
          }
        }

    );
    for (State<Example> each : fsm.states()) {
      System.out.println("dest=" + each + router.routeTo(each));
    }
  }

  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<Example, Spec> example;

  @Test
  public void test2() {
    FSMUtils.performStory(this, "example", new Example());
  }


  List<Args> possibleArgsList(final Action<Example> action) {
    final TupleGenerator tg = new IPO2TupleGenerator(2);
    tg.setFactors(action.parameters());
    tg.setConstraintManager(action.parameters().getConstraintManager());
    tg.init();
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
        }.apply(tg.get(index));
      }

      @Override
      public int size() {
        return (int) tg.size();
      }
    };
  }

}

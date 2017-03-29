package com.github.dakusui.jcunit.tests.features.fsm;

import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.ParametersSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;
import org.hamcrest.CoreMatchers;
import org.junit.experimental.theories.*;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeThat;

@RunWith(Theories.class)
public class StateRouterTest {
  @DataPoint("straightFsm")
  public static FiniteStateMachine<Sut> straightFsm = new FiniteStateMachine.Impl<Sut>("sut", StraightSpec.class);

  @DataPoints("straightFsmState")
  public static List<State<Sut>> straightFsmStates() {
    return straightFsm.states();
  }

  @DataPoint("cyclicFsm")
  public static FiniteStateMachine<Sut> cyclicFsm = new FiniteStateMachine.Impl<Sut>("sut", CyclicSpec.class);

  @DataPoints("cyclicFsmState")
  public static List<State<Sut>> cyclicFsmStates() {
    return cyclicFsm.states();
  }

  @Theory
  public void givenStraightFsmAndGoalStateIsNotInitial$whenRoute$thenSequenceEndsWithGoal(
      @FromDataPoints("straightFsmState") State<Sut> goalState,
      @FromDataPoints("straightFsm") FiniteStateMachine<Sut> fsm) {
    givenGoalStateIsNotInitial$whenRoute$thenSequenceEndsWithGoal(goalState, fsm);
  }

  @Theory
  public void givenStraightFsmAndGoalStateIsInitial$whenRoute$thenSequenceIsEmpty(
      @FromDataPoints("straightFsmState") State<Sut> goalState,
      @FromDataPoints("straightFsm") FiniteStateMachine<Sut> fsm) {
    givenGoalStateIsInitial$whenRoute$thenSequenceIsEmpty(goalState, fsm);
  }

  @Theory
  public void givenCyclicFsmAndGoalStateIsNotInitial$whenRoute$thenSequenceEndsWithGoal(
      @FromDataPoints("cyclicFsmState") State<Sut> goalState,
      @FromDataPoints("cyclicFsm") FiniteStateMachine<Sut> fsm) {
    givenGoalStateIsNotInitial$whenRoute$thenSequenceEndsWithGoal(goalState, fsm);
  }

  @Theory
  public void givenCyclicFsmAndGoalStateIsInitial$whenRoute$thenSequenceIsEmpty(
      @FromDataPoints("cyclicFsmState") State<Sut> goalState,
      @FromDataPoints("cyclicFsm") FiniteStateMachine<Sut> fsm) {
    givenGoalStateIsInitial$whenRoute$thenSequenceIsEmpty(goalState, fsm);
  }

  private void givenGoalStateIsInitial$whenRoute$thenSequenceIsEmpty(State<Sut> goalState, FiniteStateMachine<Sut> fsm) {
    assumeThat(fsm.initialState(), CoreMatchers.is(goalState));

    StateRouter<Sut> router = new StateRouter.Base<Sut>(fsm);
    ScenarioSequence<Sut> scenarioSequence = router.routeTo(goalState);
    assertEquals(0, scenarioSequence.size());
  }

  private void givenGoalStateIsNotInitial$whenRoute$thenSequenceEndsWithGoal(State<Sut> goalState, FiniteStateMachine<Sut> fsm) {
    assumeThat(fsm.initialState(), CoreMatchers.not(CoreMatchers.is(goalState)));

    StateRouter<Sut> router = new StateRouter.Base<Sut>(fsm);
    ScenarioSequence<Sut> scenarioSequence = router.routeTo(goalState);
    System.out.printf("%s: %s%n", goalState, scenarioSequence);
    assertEquals(fsm.initialState(), scenarioSequence.get(0).given);

    for (int i = 0; i < scenarioSequence.size(); i++) {
      Scenario<Sut> each = scenarioSequence.get(i);
      State<Sut> next = i == scenarioSequence.size() - 1
          ? goalState
          : scenarioSequence.get(i + 1).given;
      assertEquals(next, each.given.expectation(each.when, each.with).state);
    }
  }

  public static class Sut {
    // This method is reflectively accessed by JCUnit.
    @SuppressWarnings("unused")
    public void action0(int p1, int p2) {
    }

    // This method is reflectively accessed by JCUnit.
    @SuppressWarnings("unused")
    public void action1(int p1) {
    }
  }

  public enum StraightSpec implements FSMSpec<Sut> {
    @StateSpec I {
      @Override
      public Expectation<Sut> action1(Expectation.Builder<Sut> b, int p1, int p2) {
        if (p1 == 0)
          return b.valid(S0).build();
        return b.invalid().build();
      }
    },
    @StateSpec S0 {
      @Override
      public Expectation<Sut> action1(Expectation.Builder<Sut> b, int p1, int p2) {
        if (p1 > 1)
          return b.valid(S1).build();
        return b.invalid().build();
      }
    },
    @StateSpec S1 {
      @Override
      public Expectation<Sut> action1(Expectation.Builder<Sut> b, int p1, int p2) {
        return b.invalid().build();
      }
    };

    @ParametersSpec
    public final static Parameters action1 = new Parameters.Builder()
        .addParameter().withValues(0, 1, 2)
        .addParameter().withValues(0, 1, 2)
        .build();

    @ActionSpec
    public abstract Expectation<Sut> action1(Expectation.Builder<Sut> b, int p1, int p2);

    @Override
    public boolean check(Sut sut) {
      return true;
    }
  }

  public enum CyclicSpec implements FSMSpec<Sut> {
    @StateSpec I {
      @Override
      public Expectation<Sut> action1(Expectation.Builder<Sut> b, int p1, int p2) {
        if (p1 == 0)
          return b.valid(S0).build();
        return b.invalid().build();
      }
    },
    @StateSpec S0 {
      @Override
      public Expectation<Sut> action1(Expectation.Builder<Sut> b, int p1, int p2) {
        if (p1 > 1)
          return b.valid(S1).build();
        return b.invalid().build();
      }

      @Override
      public Expectation<Sut> action2(Expectation.Builder<Sut> b, int p1) {
        if (p1 == 2) {
          return b.valid(I).build();
        }
        return b.valid(S0).build();
      }
    },

    @StateSpec S1 {
      @Override
      public Expectation<Sut> action1(Expectation.Builder<Sut> b, int p1, int p2) {
        return b.invalid().build();
      }

      @Override
      public Expectation<Sut> action2(Expectation.Builder<Sut> b, int p1) {
        if (p1 == 2) {
          return b.valid(I).build();
        }
        return b.valid(S0).build();
      }
    };

    @ParametersSpec
    public final static Parameters action1 = new Parameters.Builder()
        .addParameter().withValues(0, 1, 2)
        .addParameter().withValues(0, 1, 2)
        .build();

    @ActionSpec
    public abstract Expectation<Sut> action1(Expectation.Builder<Sut> b, int p1, int p2);

    @ParametersSpec
    public final static Parameters action2 = new Parameters.Builder()
        .addParameter().withValues(0, 1, 2)
        .build();

    @ActionSpec
    public Expectation<Sut> action2(Expectation.Builder<Sut> b, int p1) {
      return b.invalid().build();
    }

    @Override
    public boolean check(Sut sut) {
      return true;
    }
  }

}

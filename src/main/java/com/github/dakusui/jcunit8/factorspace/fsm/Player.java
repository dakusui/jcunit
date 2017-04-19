package com.github.dakusui.jcunit8.factorspace.fsm;

import com.github.dakusui.jcunit.fsm.Action;
import com.github.dakusui.jcunit.fsm.Args;
import com.github.dakusui.jcunit.fsm.Expectation;
import com.github.dakusui.jcunit.fsm.State;

import java.util.function.Predicate;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public interface Player<SUT> {
  void visit(Stimulus<SUT> stimulus);

  void visit(Edge<SUT> edge);

  void visit(Sequence<SUT> sequence);

  void visit(Scenario<SUT> scenario);

  abstract class Base<SUT> implements Player<SUT> {
    protected final SUT sut;

    public Base(SUT sut) {
      this.sut = sut;
    }

    @Override
    public void visit(Stimulus<SUT> stimulus) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void visit(Sequence<SUT> sequence) {
      for (Edge<SUT> each : sequence) {
        each.accept(this);
      }
    }

    @Override
    public void visit(Scenario<SUT> scenario) {
      scenario.setUp().accept(this);
      scenario.main().accept(this);
    }

    public void play(Scenario<SUT> scenario) {
      if (!scenario.setUp().isEmpty())
        scenario.setUp().get(0).from.check(sut);
      visit(scenario);
    }

  }

  class Simple<SUT> extends Base<SUT> {
    public Simple(SUT sut) {
      super(sut);
    }

    @Override
    public void visit(Edge<SUT> edge) {
      Expectation<SUT> expectation = edge.from.expectation(edge.action, edge.args);
      try {
        assertThat(
            edge.action.perform(sut, edge.args),
            expectation.getType().returnedValueMatcher(getOutputChecker(sut, edge.from, edge.action, edge.args))
        );
        checkState(expectation);
      } catch (AssertionError e) {
        throw e;
      } catch (Throwable throwable) {
        assertThat(
            throwable,
            expectation.getType().thrownExceptionMatcher(getExceptionChecker(sut, edge.from, edge.action, edge.args))
        );
        checkState(expectation);
      }
    }

    private void checkState(Expectation<SUT> expectation) {
      assertTrue(
          String.format(
              "SUT '%s' is not in state '%s'",
              sut,
              expectation.state
          ),
          expectation.state.check(sut)
      );
    }

    protected Predicate<Object> getOutputChecker(SUT sut, State<SUT> from, Action<SUT> action, Args args) {
      return o -> true;
    }

    protected Predicate<Throwable> getExceptionChecker(SUT sut, State<SUT> from, Action<SUT> action, Args args) {
      return o -> true;
    }
  }
}

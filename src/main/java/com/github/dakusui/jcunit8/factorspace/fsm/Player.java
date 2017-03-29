package com.github.dakusui.jcunit8.factorspace.fsm;

import com.github.dakusui.jcunit.fsm.Expectation;

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
      ////
      // TODO
      Expectation<SUT> expectation = edge.from.expectation(edge.action, edge.args);
      try {
        edge.action.perform(sut, edge.args);
      } catch (Throwable throwable) {
        throwable.printStackTrace();
      } finally {
        expectation.state.check(sut);
      }
    }
  }
}

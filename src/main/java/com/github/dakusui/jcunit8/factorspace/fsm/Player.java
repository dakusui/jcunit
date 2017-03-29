package com.github.dakusui.jcunit8.factorspace.fsm;

public interface Player<SUT> {
  void play(Stimulus<SUT> stimulus);

  void play(Edge<SUT> edge);

  void play(Sequence<SUT> sequence);

  void play(Scenario<SUT> scenario);

  class Simple<SUT> implements Player<SUT> {

    @Override
    public void play(Stimulus<SUT> stimulus) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void play(Edge<SUT> edge) {

    }

    @Override
    public void play(Sequence<SUT> sequence) {
      for (Edge<SUT> each : sequence) {
        each.accept(this);
      }
    }

    @Override
    public void play(Scenario<SUT> scenario) {
      scenario.setUp().accept(this);
      scenario.main().accept(this);
    }
  }
}

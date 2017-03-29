package com.github.dakusui.jcunit8.factorspace.fsm;

public interface Scenario<SUT> extends Stimulus<SUT> {
  Sequence<SUT> setUp();

  Sequence<SUT> main();

  class Impl<SUT> implements Scenario<SUT> {
    private final Sequence<SUT> setUp;
    private final Sequence<SUT> main;

    public Impl(Sequence<SUT> setUp, Sequence<SUT> main) {
      this.setUp = setUp;
      this.main = main;
    }

    @Override
    public Sequence<SUT> setUp() {
      return this.setUp;
    }

    @Override
    public Sequence<SUT> main() {
      return this.main;
    }

    @Override
    public void accept(Player<SUT> player) {
      player.play(this);
    }
  }
}

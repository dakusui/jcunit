package com.github.dakusui.jcunit8.factorspace.fsm;

public interface Stimulus<SUT> {
  void accept(Player<SUT> player);
}

package com.github.dakusui.jcunit.experimentals.fsm;

import java.util.ArrayList;
import java.util.List;

public class ScenarioSequence<SUT> {
  private final List<Scenario<SUT>> seq;

  public ScenarioSequence() {
    seq = new ArrayList<Scenario<SUT>>();
  }

  public ScenarioSequence<SUT> subsequence(int begin) {
    return null;
  }

  public void append(ScenarioSequence<SUT> seq) {
  }

  public Scenario<SUT> get(int i) {
    return seq.get(i);
  }
}

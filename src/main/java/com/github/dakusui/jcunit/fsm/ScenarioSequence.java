package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;

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

  public int size() {
    return seq.size();
  }

  public void add(Scenario<SUT> scenario) {
    Checks.checknotnull(scenario);
    this.seq.add(scenario);
  }
}

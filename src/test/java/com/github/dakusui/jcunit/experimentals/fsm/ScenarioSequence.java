package com.github.dakusui.jcunit.experimentals.fsm;

import java.util.ArrayList;
import java.util.LinkedList;

public class ScenarioSequence<SUT>  {
  private final LinkedList<Scenario<SUT>> seq;

  public ScenarioSequence() {
    seq = new LinkedList<Scenario<SUT>>();
  }

  public ScenarioSequence<SUT> subsequence(int begin) {
    return new ScenarioSequence<SUT>();
  }
}

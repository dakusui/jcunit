package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;

import java.util.LinkedHashMap;

/**
 */
public class ScenarioFactorsBuilder<SUT> {
  private int length = 1;
  private FSM<SUT> fsm;
  private int      index;

  public ScenarioFactorsBuilder<SUT> setFSM(FSM<SUT> fsm) {
    Checks.checknotnull(fsm);
    this.fsm = fsm;
    return this;
  }

  public ScenarioFactorsBuilder<SUT> setLength(int length) {
    Checks.checkcond(length > 0);
    this.length = length;
    return this;
  }

  public Factors build() {
    Factors.Builder b = new Factors.Builder();
    LinkedHashMap<String, Factor> allParams = new LinkedHashMap<String, Factor>();
    for (index = 0; index < this.length; index++) {
      {
        Factor.Builder bb = new Factor.Builder();
        bb.setName(stateName(index));
        for (State each : fsm.states()) {
          bb.addLevel(each);
        }
        b.add(bb.build());
      }
      {
        Factor.Builder bb = new Factor.Builder();
        bb.setName(actionName(index));
        for (Action each : fsm.actions()) {
          bb.addLevel(each);
          for (Factor eachParam : each.params(this)) {
            if (!allParams.containsKey(eachParam.name)) {
              allParams.put(eachParam.name, eachParam);
            }
          }
        }
        b.add(bb.build());
      }
    }
    for (Factor each : allParams.values()) {
      b.add(each);
    }
    return b.build();
  }

  /**
   * Returned value of this method can be used by implementations of {@code Action} interface
   * to figure out how many times the {@code Action#params} method is called by this builder so far.
   *
   * If it is the first time, this method returns 0.
   *
   */
  public int index() {
    return this.index;
  }

  public String stateName(int i) {
    return String.format("FSM:state:%d", i);
  }

  public String actionName(int i) {
    return String.format("FSM:action:%d", i);
  }
}

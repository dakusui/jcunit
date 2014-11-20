package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;

import java.util.LinkedHashMap;
import java.util.List;

public abstract class ScenarioFactors extends Factors implements FactorNameResolver {
  public ScenarioFactors(List<Factor> factors) {
    super(factors);
  }

  /**
   */
  public static class Builder<SUT> extends Factors.Builder {
    private int length = 1;
    private FSM<SUT> fsm;
    private int      index;

    public Builder<SUT> setFSM(FSM<SUT> fsm) {
      Checks.checknotnull(fsm);
      this.fsm = fsm;
      return this;
    }

    public Builder<SUT> setLength(int length) {
      Checks.checkcond(length > 0);
      this.length = length;
      return this;
    }

    public ScenarioFactors build() {
      LinkedHashMap<String, Factor> allParams = new LinkedHashMap<String, Factor>();
      for (index = 0; index < this.length; index++) {
        {
          Factor.Builder bb = new Factor.Builder();
          bb.setName(stateName(index));
          for (State each : fsm.states()) {
            bb.addLevel(each);
          }
          this.add(bb.build());
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
          this.add(bb.build());
        }
      }
      for (Factor each : allParams.values()) {
        this.add(each);
      }
      return new ScenarioFactors(this.factors) {
        @Override
        public String stateName(int i) {
          return stateName(i);
        }

        @Override
        public String actionName(int i) {
          return actionName(i);
        }

        @Override
        public int numParams(String actionName) {
          return 0;
        }

        @Override
        public String paramName(String actionName, int i) {
          return null;
        }
      };
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
}

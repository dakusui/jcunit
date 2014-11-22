package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;

import java.util.ArrayList;
import java.util.List;

public abstract class FSMFactors extends Factors
    implements FactorNameResolver {
  public static final Object VOID = new Object();

  public FSMFactors(List<Factor> factors) {
    super(factors);
  }

  /**
   */
  public static class Builder<SUT> extends Factors.Builder {
    private int length = 1;
    private FSM<SUT> fsm;

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

    public FSMFactors build() {
      final int len = this.length;
      final int[] numParams = new int[len];
      for (int index = 0; index < len; index++) {
        {
          Factor.Builder bb = new Factor.Builder();
          bb.setName(stateName(index));
          for (State each : fsm.states()) {
            bb.addLevel(each);
          }
          this.add(bb.build());
        }
        final List<List<Object>> allParams = new ArrayList<List<Object>>();
        int smallestNumParams = Integer.MAX_VALUE;
        {
          Factor.Builder bb = new Factor.Builder();
          bb.setName(actionName(index));
          for (Action each : fsm.actions()) {
            bb.addLevel(each);
            if (each.numParams() < smallestNumParams) {
              smallestNumParams = each.numParams();
            }
            for (int i = 0; i < each.numParams(); i++) {
              if (i >= allParams.size()) {
                allParams.add(new ArrayList<Object>());
              }
              Object[] paramValues = each.param(i);
              for (Object v : paramValues) {
                if (!allParams.get(i).contains(v)) {
                  allParams.get(i).add(v);
                }
              }
            }
          }
          this.add(bb.build());
        }
        {
          numParams[index] = allParams.size();
          int i = 0;
          for (List<Object> each : allParams) {
            Factor.Builder bb = new Factor.Builder();
            bb.setName(paramName(index, i++));
            if (i >= smallestNumParams) {
              bb.addLevel(VOID);
            }
            for (Object v : each) {
              bb.addLevel(v);
            }
          }
        }
      }
      return new FSMFactors(this.factors) {
        @Override
        public String stateFactorName(int i) {
          Checks.checkcond(0 <= i);
          Checks.checkcond(i < len);
          return stateName(i);
        }

        @Override
        public String actionFactorName(int i) {
          Checks.checkcond(0 <= i);
          Checks.checkcond(i < len);
          return actionName(i);
        }

        @Override
        public int numParamFactors(int i) {
          Checks.checkcond(i >= 0);
          Checks.checkcond(i < numParams.length);
          return numParams[i];
        }

        @Override public String paramFactorName(int i, int j) {
          return paramName(i, j);
        }

        @Override
        public int size() {
          return len;
        }
      };
    }

    private String stateName(int i) {
      return String.format("FSM:state:%d", i);
    }

    private String actionName(int i) {
      return String.format("FSM:action:%d", i);
    }

    private String paramName(int i, int j) {
      return String.format("FSM:param:%d:%d", i, j);
    }
  }
}

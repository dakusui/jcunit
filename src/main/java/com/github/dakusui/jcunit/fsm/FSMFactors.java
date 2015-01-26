package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Defines factors for FSM using conventions below.
 * <p/>
 * <pre>
 * FSM:state:{i}      - {i}th state
 * FSM:action:{i}     - Action which should be performed on {i}th
 * FSM:param:{i}:{j}  - Parameters given to the {i}th action.
 * </pre>
 * Levels for FSM:param:{i}:{j} are not intuitive.
 * They are union of {j}'s arguments of all the actions.
 */
public abstract class FSMFactors extends Factors {
  public static final Object VOID = new Object();

  public FSMFactors(List<Factor> factors) {
    super(factors);
  }

  public abstract String stateFactorName(int i);

  public abstract String actionFactorName(int i);

  public abstract String paramFactorName(int i, int j);

  public abstract int historyLength();

  /**
   */
  public static class Builder<SUT> extends Factors.Builder {
    private int length = 1;
    private FSM<SUT> fsm;
    private Factors  baseFactors;

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

    public Builder<SUT> setBaseFactors(Factors baseFactors) {
      this.baseFactors = baseFactors;
      return this;
    }

    public FSMFactors build() {
      for (int index = 1; index < this.baseFactors.size(); index++) {
        this.add(this.baseFactors.get(index));
      }

      final int len = this.length;
      for (int index = 0; index < len; index++) {
        ////
        // Build a factor for {index}th state
        {
          Factor.Builder bb = new Factor.Builder();
          bb.setName(stateName(index));
          for (State each : fsm.states()) {
            bb.addLevel(each);
          }
          this.add(bb.build());
        }
        ////
        // Build a factor for {index}th action
        // {i}th element of allParams (List<Object>) is a list of possible levels
        //
        final List<Set<Object>> allParams = new ArrayList<Set<Object>>();
        int smallestNumParams = Integer.MAX_VALUE;
        {
          Factor.Builder bb = new Factor.Builder();
          bb.setName(actionName(index));
          for (Action each : fsm.actions()) {
            bb.addLevel(each);
            if (each.numParameterFactors() < smallestNumParams)
              smallestNumParams = each.numParameterFactors();
            for (int i = 0; i < each.numParameterFactors(); i++) {
              if (i >= allParams.size()) {
                allParams.add(new LinkedHashSet<Object>());
              }
              Object[] paramValues = each.parameterFactorLevels(i);
              for (Object v : paramValues) {
                allParams.get(i).add(v);
              }
            }
          }
          this.add(bb.build());
        }
        ////
        // Build factors for {index}th action's parameters
        {
          int i = 0;
          for (Set<Object> each : allParams) {
            Factor.Builder bb = new Factor.Builder();
            bb.setName(paramName(index, i++));
            if (i >= smallestNumParams) {
              bb.addLevel(VOID);
              continue;
            }
            for (Object v : each) {
              bb.addLevel(v);
            }
            this.add(bb.build());
          }
        }
      }
      return new FSMFactors(this.factors) {
        public String stateFactorName(int i) {
          Checks.checkcond(0 <= i);
          Checks.checkcond(i < len);
          return stateName(i);
        }

        public String actionFactorName(int i) {
          Checks.checkcond(0 <= i);
          Checks.checkcond(i < len);
          return actionName(i);
        }

        public String paramFactorName(int i, int j) {
          return paramName(i, j);
        }

        public int historyLength() {
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

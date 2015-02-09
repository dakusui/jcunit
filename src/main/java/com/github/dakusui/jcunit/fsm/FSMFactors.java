package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;

import java.util.*;

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
public class FSMFactors extends Factors {
  public static final Object VOID = new Object();

  private final Map<String, FSM<?>> fsmMap;

  protected FSMFactors(List<Factor> factors, Map<String, FSM<?>> fsmMap) {
    super(factors);
    this.fsmMap = Collections.unmodifiableMap(fsmMap);
  }

  private static String stateName(String fsmName, int i) {
    return String.format("FSM:%s:state:%d", fsmName, i);
  }

  private static String actionName(String fsmName, int i) {
    return String.format("FSM:%s:action:%d", fsmName, i);
  }

  private static String paramName(String fsmName, int i, int j) {
    return String.format("FSM:%s:param:%d:%d", fsmName, i, j);
  }

  public List<String> getFSMNames() {
    List<String> ret = new ArrayList<String>(this.fsmMap.size());
    ret.addAll(this.fsmMap.keySet());
    return Collections.unmodifiableList(ret);
  }

  public String stateFactorName(String fsmName, int i) {
    Checks.checknotnull(fsmName);
    Checks.checkcond(this.fsmMap.get(fsmName) != null);
    Checks.checkcond(0 <= i);
    Checks.checkcond(i < historyLength(fsmName));
    return stateName(fsmName, i);
  }

  public String actionFactorName(String fsmName, int i) {
    Checks.checknotnull(fsmName);
    Checks.checkcond(this.fsmMap.get(fsmName) != null);
    Checks.checkcond(0 <= i);
    Checks.checkcond(i < historyLength(fsmName));
    return actionName(fsmName, i);
  }

  public String paramFactorName(String fsmName, int i, int j) {
    Checks.checknotnull(fsmName);
    Checks.checkcond(this.fsmMap.get(fsmName) != null);
    return paramName(fsmName, i, j);
  }

  public int historyLength(String fsmName) {
    Checks.checknotnull(fsmName);
    Checks.checkcond(this.fsmMap.get(fsmName) != null);
    return this.fsmMap.get(fsmName).historyLength();
  }

  /**
   */
  public static class Builder extends Factors.Builder {
    private Map<String, FSM<?>> fsms = new LinkedHashMap<String, FSM<?>>();
    private Factors baseFactors;

    public Builder addFSM(String name, FSM<?> fsm) {
      Checks.checknotnull(fsm);
      this.fsms.put(name, fsm);
      return this;
    }

    public Builder setBaseFactors(Factors baseFactors) {
      this.baseFactors = baseFactors;
      return this;
    }

    public FSMFactors build() {
      for (int index = 1; index < this.baseFactors.size(); index++) {
        this.add(this.baseFactors.get(index));
      }

      for (Map.Entry<String, FSM<?>> entry : fsms.entrySet()) {
        String fsmName = entry.getKey();
        FSM<?> fsm = entry.getValue();
        int len = fsm.historyLength();
        for (int index = 0; index < len; index++) {
          ////
          // Build a factor for {index}th state
          {
            Factor.Builder bb = new Factor.Builder();
            bb.setName(stateName(fsmName, index));
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
            bb.setName(actionName(fsmName, index));
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
              bb.setName(paramName(fsmName, index, i++));
              if (i >= smallestNumParams)
                bb.addLevel(VOID);
              for (Object v : each) {
                bb.addLevel(v);
              }
              this.add(bb.build());
            }
          }
        }
      }
      return new FSMFactors(this.factors, this.fsms);
    }
  }
}

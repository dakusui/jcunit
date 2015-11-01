package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;

import java.util.*;

/**
 * Defines factors for FSM(s) using conventions below.
 * A builder of this class creates an adapter for a regular {@code Factors} object,
 * which is this class, {@code FSMFactors}, itself.
 *
 * The builder internally generates following factors from an {@code FSM} object
 * given to it.
 * <p/>
 * <code>
 * FSM:{FSM name}:state:{i}      - {i}th state
 * FSM:{FSM name}:action:{i}     - Action which should be performed on {i}th
 * FSM:{FSM name}:param:{i}:{j}  - Parameters given to the {i}th action.
 * </code>
 * Levels for FSM:param:{i}:{j} are not intuitive.
 * They are union of {j}'s arguments of all the actions.
 *
 * As the spec above implies, an object of this class can hold multiple FSMs in it.
 *
 * This class provides some useful methods like {@code stateName()}, etc, that
 * return the FSM's information in an abstract way.
 */
public class FSMFactors extends Factors {
  public static final Object VOID = new Object();

  private final Map<String, FSM<?>> fsmMap;

  protected FSMFactors(List<Factor> factors, Map<String, FSM<?>> fsmMap) {
    super(factors);
    this.fsmMap = Collections.unmodifiableMap(fsmMap);
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

  public static String stateName(String fsmName, int i) {
    return String.format("FSM:%s:state:%d", fsmName, i);
  }

  public static String actionName(String fsmName, int i) {
    return String.format("FSM:%s:action:%d", fsmName, i);
  }

  public static String paramName(String fsmName, int i, int j) {
    return String.format("FSM:%s:param:%d:%d", fsmName, i, j);
  }

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

    @Override
    public FSMFactors build() {
      Set<String> processedFSMfactors = new HashSet<String>();
      for (Map.Entry<String, FSM<?>> entry : fsms.entrySet()) {
        String fsmName = entry.getKey();
        processedFSMfactors.add(fsmName);
        FSM<?> fsm = entry.getValue();
        int len = fsm.historyLength();
        for (int index = 0; index < len; index++) {
          ////
          // Build a factor for {index}th state
          {
            Factor.Builder bb = new Factor.Builder(stateName(fsmName, index));
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
          ////
          // 'smallestNumParams' holds the smallest number of parameters of
          // 'action' methods.
          // All the actions share the same parameter factors.
          // This means some parameter factors (e.g., the last one) will not be
          // used sometimes unless corresponding action's level is set to the method
          // with the most parameters.
          int smallestNumParams = Integer.MAX_VALUE;
          {
            Factor.Builder bb = new Factor.Builder(actionName(fsmName, index));
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
              Factor.Builder bb = new Factor.Builder(paramName(fsmName, index, i++));
              if (i >= smallestNumParams)
                ////
                // Add VOID action as a level. 'smallestNumParams' is the number of
                // parameters of the method with the least parameters.
                // Parameter factors after this point must have VOID level.
                // Because if a method whose parameters are little than the largest,
                // it means the last some parameters cannot have any arguments.
                bb.addLevel(VOID);
              for (Object v : each) {
                bb.addLevel(v);
              }
              this.add(bb.build());
            }
          }
        }
      }
      for (int index = 0; index < this.baseFactors.size(); index++) {
        if (!processedFSMfactors.contains(this.baseFactors.get(index).name)) {
          this.add(this.baseFactors.get(index));
        }
      }
      return new FSMFactors(this.factors, this.fsms);
    }
  }
}

package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;

import java.util.*;

import static com.github.dakusui.jcunit.core.Checks.checkcond;
import static com.github.dakusui.jcunit.core.Checks.checknotnull;
import static com.github.dakusui.jcunit.core.factor.FactorDef.Fsm.*;

/**
 * Defines factors for FSM(s) using conventions below.
 * A builder of this class creates an adapter for a regular {@code Factors} object,
 * which is this class, {@code FSMFactors}, itself.
 * <p/>
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
 * <p/>
 * As the spec above implies, an object of this class can hold multiple FSMs in it.
 * <p/>
 * This class provides some useful methods like {@code stateName()}, etc, that
 * return the FSM's information in an abstract way.
 */
public class FSMFactors extends Factors {
  public static final Object VOID = new Object();
  private final String name;


  private FSMFactors(String name, List<Factor> fsmFactors) {
    super(fsmFactors);
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  public static class Builder extends Factors.Builder {
    private final String fsmName;
    private final FSM<?> fsm;
    private final int historyLength;

    public Builder(String fsmName, FSM<?> fsm, int historyLength) {
      this.fsmName = checknotnull(fsmName);
      this.fsm = checknotnull(fsm);
      checkcond(historyLength > 0);
      this.historyLength = historyLength;
    }

    @Override
    public FSMFactors build() {
      String fsmName = this.fsmName;
      FSM<?> fsm = this.fsm;
      for (int index = 0; index < this.historyLength; index++) {
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
      return new FSMFactors(this.fsmName, this.factors);
    }
  }
}

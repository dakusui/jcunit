package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.constraints.Constraint;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintBase;

import java.util.Collections;
import java.util.List;

/**
 * A constraint which validates tuples for FSM scenarios.
 * <p/>
 * An instance of this object is created only by {@see ToplevelCAEngine}.
 */
public class FSMConstraint<SUT> extends ConstraintBase {
  private final Constraint                       baseConstraint;
  private final List<Parameters.LocalConstraint> localCMs;

  /**
   * Creates an object of this class.
   *
   * @param baseCM A constraint manager which validates 'non-FSM' attributes.
   */
  public FSMConstraint(Constraint baseCM, List<Parameters.LocalConstraint> localCMS) {
    super();
    Checks.checknotnull(baseCM);
    this.baseConstraint = baseCM;
    this.localCMs = Collections.unmodifiableList(Checks.checknotnull(localCMS));
  }

  @Override
  public boolean check(Tuple tuple) throws UndefinedSymbol {
    if (!this.baseConstraint.check(tuple))
      return false;
    FSMFactors fsmFactors = (FSMFactors) this.getFactors();
    for (String each : fsmFactors.getFSMNames()) {
      ScenarioSequence<SUT> seq = new ScenarioSequence.BuilderFromTuple<SUT>()
          .setFSMFactors(fsmFactors)
          .setTuple(tuple)
          .setFSMName(each)
          .build();
      State<SUT> expectedState = null;
      for (int i = 0; i < fsmFactors.historyLength(each); i++) {
        State<SUT> state = seq.state(i);
        if (state == null)
          throw new UndefinedSymbol(new String[] { fsmFactors.stateFactorName(each, i) });
        if (expectedState != null) {
          if (expectedState != state)
            return false;
        }

        Action<SUT> action = seq.action(i);
        if (action == null)
          throw new UndefinedSymbol(new String[] { fsmFactors.actionFactorName(each, i) });
        if (state == State.Void.getInstance()) {
          if (action != Action.Void.getInstance())
            return false;
        }
        int numParams = action.numParameterFactors();
        for (int j = 0; j < numParams; j++) {
          if (!seq.hasArg(i, j))
            throw new UndefinedSymbol(new String[] { fsmFactors.paramFactorName(each, i, j) });
          if (j >= numParams) {
            if (seq.arg(i, j) != FSMFactors.VOID)
              return false;
          }
          if (!isPossible(action.parameterFactorLevels(j), seq.arg(i, j))) {
            return false;
          }
        }

        Args args = seq.args(i);
        Expectation<SUT> expectation = state.expectation(action, args);
        if (expectation == null)
          return false;
        expectedState = expectation.state;
      }
    }
    for (Parameters.LocalConstraint each : this.localCMs) {
      if (!each.check(tuple)) {
        return false;
      }
    }
    return true;
  }

  private boolean isPossible(Object[] possibleValues, Object value) {
    for (Object pv : possibleValues) {
      if (Utils.eq(pv, value))
        return true;
    }
    return false;
  }
}

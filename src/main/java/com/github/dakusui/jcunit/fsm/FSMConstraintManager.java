package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.constraintmanagers.ConstraintManager;
import com.github.dakusui.jcunit.plugins.constraintmanagers.ConstraintManagerBase;

import java.util.Collections;
import java.util.List;

/**
 * A constraint manager which validates tuples which describes an FSM scenario.
 * <p/>
 * An instance of this object is created only by {@code FSMTupleGenerator}.
 */
public class FSMConstraintManager<SUT> extends ConstraintManagerBase {
  private final ConstraintManager                       baseConstraintManager;
  private final List<Parameters.LocalConstraintManager> localCMs;

  /**
   * Creates an object of this class.
   *
   * @param baseCM A constraint manager which validates 'non-FSM' attributes.
   */
  public FSMConstraintManager(ConstraintManager baseCM, List<Parameters.LocalConstraintManager> localCMS) {
    super();
    Checks.checknotnull(baseCM);
    this.baseConstraintManager = baseCM;
    this.localCMs = Collections.unmodifiableList(Checks.checknotnull(localCMS));
  }

  @Override
  public boolean check(Tuple tuple) throws UndefinedSymbol {
    if (!this.baseConstraintManager.check(tuple))
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
        if (state == State.VOID) {
          if (action != Action.VOID)
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
    for (Parameters.LocalConstraintManager each : this.localCMs) {
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

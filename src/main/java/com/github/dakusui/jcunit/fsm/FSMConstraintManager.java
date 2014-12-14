package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.constraint.constraintmanagers.ConstraintManagerBase;
import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;

/**
 * A constraint manager which validates tuples which describes an FSM scenario.
 *
 * An instance of this object is created only by {@code ScenarioTupleGenerator}.
 */
public class FSMConstraintManager<SUT> extends ConstraintManagerBase {
  private final ConstraintManager baseConstraintManager;

  /**
   * Creates an object of this class.
   *
   * @param baseCM A constraint manager which validates 'non-FSM' attributes.
   */
  FSMConstraintManager(ConstraintManager baseCM) {
    Checks.checknotnull(baseCM);
    this.baseConstraintManager = baseCM;
  }

  @Override
  public boolean check(Tuple tuple) throws UndefinedSymbol {
    if (!this.baseConstraintManager.check(tuple)) return false;
    FSMFactors fsmFactors = (FSMFactors) this.getFactors();
    ScenarioSequence<SUT> seq = new ScenarioSequence.BuilderFromTuple<SUT>()
        .setFSMFactors(fsmFactors).setTuple(tuple).build();
    State<SUT> expectedState = null;
    for (int i = 0; i < fsmFactors.historyLength(); i++) {
      State<SUT> state = seq.state(i);
      if (state == null) throw new UndefinedSymbol();
      if (expectedState != null) {
        if (expectedState != state) return false;
      }

      Action<SUT> action = seq.action(i);
      if (action == null) throw new UndefinedSymbol();
      if (state == State.VOID) {
        if (action != Action.VOID) return false;
      }
      int numParams = action.numParams();
      for (int j = 0; j < fsmFactors.numParamFactors(i); j++) {
        if (!seq.hasArg(i, j)) throw new UndefinedSymbol();
        if (j >= numParams) {
          if (seq.arg(i, j) != FSMFactors.VOID) return false;
        }
        if (!isPossible(action.param(j), seq.arg(i, j))) {
          return false;
        }
      }

      Args args = seq.args(i);
      Expectation<SUT> expectation = state.expectation(action, args);
      if (expectation == null) return false;
      expectedState = expectation.state;
    }
    return true;
  }

  private boolean isPossible(Object[] possibleValues, Object value) {
    for (Object pv : possibleValues) {
      if (Utils.eq(pv, value)) return true;
    }
    return false;
  }
}

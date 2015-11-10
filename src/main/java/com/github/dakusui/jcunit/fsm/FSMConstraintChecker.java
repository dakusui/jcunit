package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.FactorDef;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;

import java.util.Collections;
import java.util.List;

/**
 * A constraint which validates tuples for FSM scenarios.
 * <p/>
 * An instance of this object is created only by {@see ToplevelCAEngine}.
 */
public class FSMConstraintChecker<SUT> extends ConstraintChecker.Base {
  private final ConstraintChecker                       baseConstraintChecker;
  private final List<Parameters.LocalConstraintChecker> localCMs;

  /**
   * Creates an object of this class.
   *
   * @param baseCM A constraint manager which validates 'non-FSM' attributes.
   */
  public FSMConstraintChecker(ConstraintChecker baseCM, List<Parameters.LocalConstraintChecker> localCMS) {
    super();
    Checks.checknotnull(baseCM);
    this.baseConstraintChecker = baseCM;
    this.localCMs = Collections.unmodifiableList(Checks.checknotnull(localCMS));
  }

  @Override
  public boolean check(Tuple tuple) throws UndefinedSymbol {
    if (!this.baseConstraintChecker.check(tuple))
      return false;
    if (!checkTuple(tuple))
      return false;
    for (Parameters.LocalConstraintChecker each : this.localCMs) {
      if (!each.check(tuple)) {
        return false;
      }
    }
    return true;
  }

  public boolean checkTuple(Tuple tuple) throws UndefinedSymbol {
    FSMFactors fsmFactors = (FSMFactors) this.getFactors();
    for (String each : fsmFactors.getFSMNames()) {
      ScenarioSequence<SUT> seq = new ScenarioSequence.BuilderFromTuple<SUT>()
          .setFSMFactors(fsmFactors)
          .setTuple(tuple)
          .setFSMName(each)
          .build();
      if (!checkFSM(each, seq))
        return false;
    }
    return true;
  }

  public static <SUT> boolean checkFSM(String fsmName, ScenarioSequence<SUT> seq) throws UndefinedSymbol {
    State<SUT> expectedState = null;
    for (int i = 0; i < seq.size(); i++) {
      State<SUT> state = seq.state(i);
      if (state == null)
        throw new UndefinedSymbol(new String[] { FactorDef.Fsm.stateName(fsmName, i) });
      if (expectedState != null) {
        if (expectedState != state)
          return false;
      }

      Action<SUT> action = seq.action(i);
      if (action == null)
        throw new UndefinedSymbol(new String[] { FactorDef.Fsm.actionName(fsmName, i) });
      if (state == State.Void.getInstance()) {
        if (action != Action.Void.getInstance())
          return false;
      }
      int numParams = action.numParameterFactors();
      for (int j = 0; j < numParams; j++) {
        if (!seq.hasArg(i, j))
          throw new UndefinedSymbol(new String[] { FactorDef.Fsm.paramName(fsmName, i, j) });
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
    return true;
  }

  private static boolean isPossible(Object[] possibleValues, Object value) {
    for (Object pv : possibleValues) {
      if (Utils.eq(pv, value))
        return true;
    }
    return false;
  }
}

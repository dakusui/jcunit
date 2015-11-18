package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.FactorDef;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;

import java.util.Collections;
import java.util.List;

import static com.github.dakusui.jcunit.core.Checks.checknotnull;

/**
 * A constraint which validates tuples for FSM scenarios.
 * <p/>
 * An instance of this object is created only by {@see ToplevelCAEngine}.
 */
public class FSMConstraintChecker<SUT> extends ConstraintChecker.Base {
  private final List<Parameters.LocalConstraintChecker> localCMs;
  private final FSMFactors                              factors;
  private final String                                  fsmName;

  /**
   * Creates an object of this class.
   */
  public FSMConstraintChecker(String fsmName, FSMFactors factors, List<Parameters.LocalConstraintChecker> localCMS) {
    super();
    this.fsmName = checknotnull(fsmName);
    this.localCMs = Collections.unmodifiableList(checknotnull(localCMS));
    this.factors = checknotnull(factors);
  }

  @Override
  public boolean check(Tuple tuple) throws UndefinedSymbol {
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
    FSMFactors fsmFactors = this.getFactors();
    ScenarioSequence<SUT> seq = new ScenarioSequence.BuilderFromTuple<SUT>()
        .setFSMFactors(fsmFactors)
        .setTuple(tuple)
        .setFSMName(this.fsmName)
        .build();
    return checkFSM(this.fsmName, seq);
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

  public FSMFactors getFactors() {
    return factors;
  }
}

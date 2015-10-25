package com.github.dakusui.jcunit.ututils.tuples;

import com.github.dakusui.jcunit.plugins.constraintmanagers.ConstraintManager;
import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;

import java.util.HashSet;
import java.util.Set;

public class ValidTuplesCoveredExpectation extends PresenceExpectation {
  public ValidTuplesCoveredExpectation(Factors factors, int strength, ConstraintManager constraintManager) {
    super(composeExpectedTuples(factors, strength, constraintManager));
  }

  public static Set<Tuple> composeExpectedTuples(Factors factors, int strength, ConstraintManager constraintManager) {
    Checks.checknotnull(factors);
    Checks.checkcond(strength > 0);
    Checks.checknotnull(constraintManager);
    Set<Tuple> ret = new HashSet<Tuple>();
    for (Tuple t : factors.generateAllPossibleTuples(strength)) {
      try {
        if (!(constraintManager.check(t))) {
          continue;
        }
      } catch (UndefinedSymbol e) {
        ////
        // Do nothing
      }
      ret.add(t);
    }
    return ret;
  }
}

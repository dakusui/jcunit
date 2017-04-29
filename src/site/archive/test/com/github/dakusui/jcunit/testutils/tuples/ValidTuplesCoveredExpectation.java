package com.github.dakusui.jcunit.testutils.tuples;

import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;

import java.util.HashSet;
import java.util.Set;

public class ValidTuplesCoveredExpectation extends PresenceExpectation {
  public ValidTuplesCoveredExpectation(Factors factors, int strength, ConstraintChecker constraintChecker) {
    super(composeExpectedTuples(factors, strength, constraintChecker));
  }

  public static Set<Tuple> composeExpectedTuples(Factors factors, int strength, ConstraintChecker constraintChecker) {
    Checks.checknotnull(factors);
    Checks.checkcond(strength > 0);
    Checks.checknotnull(constraintChecker);
    Set<Tuple> ret = new HashSet<Tuple>();
    for (Tuple t : factors.generateAllPossibleTuples(strength)) {
      try {
        if (!(constraintChecker.check(t))) {
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

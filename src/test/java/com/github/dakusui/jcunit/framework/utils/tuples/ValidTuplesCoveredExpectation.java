package com.github.dakusui.jcunit.framework.utils.tuples;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.JCUnitSymbolException;
import com.github.dakusui.jcunit.framework.utils.tuples.PresenceExpectation;

import java.util.HashSet;
import java.util.Set;

public class ValidTuplesCoveredExpectation extends PresenceExpectation {
  public ValidTuplesCoveredExpectation(Factors factors, int strength, ConstraintManager constraintManager) {
    super(composeExpectedTuples(factors, strength, constraintManager));
  }

  public static Set<Tuple> composeExpectedTuples(Factors factors, int strength, ConstraintManager constraintManager) {
    Utils.checknotnull(factors);
    Utils.checkcond(strength > 0);
    Utils.checknotnull(constraintManager);
    Set<Tuple> ret = new HashSet<Tuple>();
    for (Tuple t : factors.generateAllPossibleTuples(strength)) {
      try {
        if (!(constraintManager.check(t))) {
          continue;
        }
      } catch (JCUnitSymbolException e) {
        ////
        // Do nothing
      }
      ret.add(t);
    }
    return ret;
  }
}

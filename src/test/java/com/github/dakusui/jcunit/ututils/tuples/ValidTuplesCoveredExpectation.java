package com.github.dakusui.jcunit.ututils.tuples;

import com.github.dakusui.jcunit.plugins.constraints.Constraint;
import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;

import java.util.HashSet;
import java.util.Set;

public class ValidTuplesCoveredExpectation extends PresenceExpectation {
  public ValidTuplesCoveredExpectation(Factors factors, int strength, Constraint constraint) {
    super(composeExpectedTuples(factors, strength, constraint));
  }

  public static Set<Tuple> composeExpectedTuples(Factors factors, int strength, Constraint constraint) {
    Checks.checknotnull(factors);
    Checks.checkcond(strength > 0);
    Checks.checknotnull(constraint);
    Set<Tuple> ret = new HashSet<Tuple>();
    for (Tuple t : factors.generateAllPossibleTuples(strength)) {
      try {
        if (!(constraint.check(t))) {
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

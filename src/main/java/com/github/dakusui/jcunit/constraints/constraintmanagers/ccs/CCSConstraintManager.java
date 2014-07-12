package com.github.dakusui.jcunit.constraints.constraintmanagers.ccs;

import com.github.dakusui.jcunit.constraints.ConstraintManagerBase;
import com.github.dakusui.jcunit.core.Tuple;
import com.github.dakusui.jcunit.core.factor.Factors;

/**
 */
public class CCSConstraintManager extends ConstraintManagerBase {
  private Factors factors;

  public CCSConstraintManager() {
  }

  /**
   * Returns {@code null} if the given tuple doesn't violate constraint rules
   * explicitly.
   * If this method can't determine if the tuple doesn't violate them because
   * it lacks values of necessary attributes, it returns {@code null}, too.
   * <p/>
   * If and only if this method finds that the given tuple violates known constraints,
   * it returns a tuple which has attributes and their values involved in the
   * evaluation.
   *
   * @param tuple The tuple to be evaluated.
   * @return A tuple which has involved values in {@code tuple}.
   */
  Tuple checkTupleWithRules(Tuple tuple) {
    return null;
  }

  @Override
  public boolean check(Tuple cand) {
    return false;
  }

  static class PossibleImplicitConstraint {

  }
}

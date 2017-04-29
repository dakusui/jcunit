package com.github.dakusui.jcunit.testutils.tuples;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;

import java.util.Collection;
import java.util.Set;

public class PresenceExpectation extends ExpectationBase {
  public PresenceExpectation(Collection<Tuple> expected) {
    super(expected);
  }

  protected boolean isSatisfiedBy(Tuple t, Set<Tuple> tuples) {
    for (Tuple u : tuples) {
      if (TupleUtils.isSubtupleOf(t, u)) {
        return true;
      }
    }
    return false;
  }

  protected String getMessage() {
    return "One or more expected tuples not found in actual tuples.";
  }
}

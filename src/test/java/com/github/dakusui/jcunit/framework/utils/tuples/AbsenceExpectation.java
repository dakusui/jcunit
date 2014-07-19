package com.github.dakusui.jcunit.framework.utils.tuples;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.framework.utils.tuples.ExpectationBase;

import java.util.Collection;
import java.util.Set;

public class AbsenceExpectation extends ExpectationBase {
  public AbsenceExpectation(Collection<Tuple> expected) {
    super(expected);
  }

  @Override
  protected boolean isSatisfiedBy(Tuple t, Set<Tuple> tuples) {
    for (Tuple u : tuples) {
      if (TupleUtils.isSubtupleOf(t, u)) {
        return false;
      }
    }
    return true;
  }

  protected String getMessage() {
    return "One or more NOT expected tuples found in actual tuples.";
  }
}

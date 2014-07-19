package com.github.dakusui.jcunit.framework.utils.tuples;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.*;

public abstract class ExpectationBase implements Expectation {
  private final Set<Tuple> expected;

  ExpectationBase(Collection<Tuple> expected) {
    this.expected = Collections.unmodifiableSet(new HashSet<Tuple>(Utils.checknotnull(expected)));
  }

  @Override
  public VerificationResult verify(List<Tuple> tuples) {
    Set<Tuple> tupleSet = new HashSet<Tuple>(tuples);
    Set<Tuple> violations = new HashSet<Tuple>();
    for (Tuple t : Utils.checknotnull(this.getExpected())) {
      if (!isSatisfiedBy(t, tupleSet)) {
        violations.add(t);
      }
    }
    return new VerificationResult(getMessage(), violations);
  }

  protected abstract String getMessage();

  protected abstract boolean isSatisfiedBy(Tuple t, Set<Tuple> tupleSet);

  protected Set<Tuple> getExpected() {
    return this.expected;
  }
}

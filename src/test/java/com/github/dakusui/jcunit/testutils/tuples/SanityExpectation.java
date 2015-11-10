package com.github.dakusui.jcunit.testutils.tuples;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SanityExpectation implements Expectation {
  private final Factors factors;

  public SanityExpectation(Factors factors) {
    this.factors = factors;
  }

  @Override
  public VerificationResult verify(List<Tuple> tuples) {
    Set<Tuple> violations = new HashSet<Tuple>();
    for (Tuple t : Checks.checknotnull(tuples)) {
      if (t == null) {
        violations.add(t);
        continue;
      }
      if (!this.factors.getFactorNames().containsAll(t.keySet())) {
        violations.add(t);
      }
      if (!t.keySet().containsAll(this.factors.getFactorNames())) {
        violations.add(t);
      }
      for (String k : t.keySet()) {
        if (!(this.factors.has(k)) || !(this.factors.get(k).levels.contains(t.get(k)))) {
          violations.add(t);
          break;
        }
      }
    }
    return new VerificationResult("One or more tuples do not fit. (Schema violation)", violations);
  }
}

package com.github.dakusui.jcunit.ututils.tuples;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class PredicateExpectation implements Expectation {
  protected final Predicate predicate;

  public PredicateExpectation(Predicate p) {
    this.predicate = Checks.checknotnull(p);
  }

  public static interface Predicate {
    boolean evaluate(Tuple t);
  }

  public static PredicateExpectation exists(Predicate p) {
    return new PredicateExpectation(p) {
      @Override
      public VerificationResult verify(List<Tuple> tuples) {
        Set<Tuple> invalidTuples = new HashSet<Tuple>();
        for (Tuple t : tuples) {
          if (this.predicate.evaluate(t)) {
            invalidTuples.add(t);
          }
        }
        return new VerificationResult("No tuple satisfying '%s' is found.", invalidTuples);
      }
    };
  }


  public static PredicateExpectation any(Predicate p) {
    return new PredicateExpectation(p) {
      @Override
      public VerificationResult verify(List<Tuple> tuples) {
        Set<Tuple> invalidTuples = new HashSet<Tuple>();
        for (Tuple t : tuples) {
          if (!this.predicate.evaluate(t)) {
            invalidTuples.add(t);
          }
        }
        return new VerificationResult("Tuples not satisfying '%s' are found.", invalidTuples);
      }
    };
  }
}

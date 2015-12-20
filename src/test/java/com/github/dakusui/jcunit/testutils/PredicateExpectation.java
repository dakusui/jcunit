package com.github.dakusui.jcunit.testutils;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.testutils.tuples.Expectation;
import com.github.dakusui.jcunit.testutils.tuples.VerificationResult;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class PredicateExpectation implements Expectation {
  protected final Predicate predicate;

  public PredicateExpectation(Predicate p) {
    this.predicate = Checks.checknotnull(p);
  }

  public static PredicateExpectation exists(final Predicate p) {
    return new PredicateExpectation(p) {
      @Override public VerificationResult verify(List<Tuple> tuples) {
        for (Tuple t : tuples) {
          if (this.predicate.evaluate(t)) {
            return new VerificationResult(true);
          }
        }
        return new VerificationResult(
            String.format("No tuple satisfying '%s' was found.", p), false);
      }
    };
  }

  public static PredicateExpectation any(final Predicate p) {
    return new PredicateExpectation(p) {
      @Override public VerificationResult verify(List<Tuple> tuples) {
        Set<Tuple> invalidTuples = new HashSet<Tuple>();
        for (Tuple t : tuples) {
          if (!this.predicate.evaluate(t)) {
            invalidTuples.add(t);
          }
        }
        return new VerificationResult("Tuples not satisfying '%s' were found.", invalidTuples);
      }
    };
  }

  public interface Predicate {
    boolean evaluate(Tuple tuple);
  }
}

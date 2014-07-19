package com.github.dakusui.jcunit.framework.utils.tuples;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.JCUnitSymbolException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NoConstraintViolationExpectation implements Expectation {
  private final ConstraintManager cm;

  public NoConstraintViolationExpectation(ConstraintManager cm) {
    this.cm = Utils.checknotnull(cm);
  }

  @Override
  public VerificationResult verify(List<Tuple> tuples) {
    Set<Tuple> invalidTuples = new HashSet<Tuple>();
    for (Tuple t : tuples) {
      try {
        if (cm.check(t)) {
          invalidTuples.add(t);
        }
      } catch (JCUnitSymbolException e) {
        ////
        // Ignore short tuples.
      }
    }
    return new VerificationResult(String.format("Invalid tuples for '%s' was found.", this.cm), invalidTuples);
  }
}

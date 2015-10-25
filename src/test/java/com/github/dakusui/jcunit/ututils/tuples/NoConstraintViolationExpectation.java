package com.github.dakusui.jcunit.ututils.tuples;

import com.github.dakusui.jcunit.plugins.constraintmanagers.ConstraintManager;
import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NoConstraintViolationExpectation implements Expectation {
  private final ConstraintManager cm;

  public NoConstraintViolationExpectation(ConstraintManager cm) {
    this.cm = Checks.checknotnull(cm);
  }

  @Override
  public VerificationResult verify(List<Tuple> tuples) {
    Set<Tuple> invalidTuples = new HashSet<Tuple>();
    for (Tuple t : tuples) {
      try {
        if (!cm.check(t)) {
          invalidTuples.add(t);
        }
      } catch (UndefinedSymbol e) {
        ////
        // Ignore short tuples.
      }
    }
    return new VerificationResult(String.format("Invalid tuples for '%s' was found.", this.cm), invalidTuples);
  }
}

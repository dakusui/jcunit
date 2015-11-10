package com.github.dakusui.jcunit.plugins.constraints;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.Plugin;

import java.util.Collections;
import java.util.List;

public interface ConstraintChecker extends Plugin {
  ConstraintChecker DEFAULT_CONSTRAINT_CHECKER = new NullConstraintChecker();

  /**
   * Returns {@code true} if the given tuple doesn't violate any known constraints.
   * In case tuple doesn't have sufficient attribute values to be evaluated,
   * an {@code UndefinedSymbol} will be thrown.
   * Otherwise, {@code false} should be returned.
   *
   * @param tuple A tuple to be evaluated.
   * @return {@code true} - The tuple doesn't violate any constraints managed by this object / {@code false} - The tuple DOES violate a constraint.
   * @throws com.github.dakusui.jcunit.exceptions.UndefinedSymbol Failed to evaluate the tuple for insufficient attribute(s).
   */
  boolean check(Tuple tuple) throws UndefinedSymbol;

  List<Tuple> getViolations();

  abstract class Base implements ConstraintChecker, Plugin {
    public Base() {
    }

    @Override
    public List<Tuple> getViolations() {
      return Collections.emptyList();
    }
  }
}

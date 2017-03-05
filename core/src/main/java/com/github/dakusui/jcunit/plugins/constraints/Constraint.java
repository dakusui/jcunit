package com.github.dakusui.jcunit.plugins.constraints;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;

import java.util.List;

public interface Constraint {
  boolean check(Tuple tuple) throws UndefinedSymbol;

  String tag();

  /**
   * Returns names of factors used by this constraint.
   */
  List<String> getFactorNamesInUse();
}

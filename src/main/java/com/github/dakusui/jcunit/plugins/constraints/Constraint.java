package com.github.dakusui.jcunit.plugins.constraints;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;

public interface Constraint {
  boolean check(Tuple tuple) throws UndefinedSymbol;

  String tag();
}

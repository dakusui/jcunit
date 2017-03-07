package com.github.dakusui.jcunit8.core;

import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.List;
import java.util.OptionalInt;

public interface Requirement {
  OptionalInt strength();
  boolean negativeTests();
  List<Tuple> seeds();
}

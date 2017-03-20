package com.github.dakusui.jcunit8.factorspace;

import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.List;
import java.util.function.Predicate;

public interface Constraint extends Predicate<Tuple> {
  boolean test(Tuple testObject);

  List<String> involvedKeys();
}

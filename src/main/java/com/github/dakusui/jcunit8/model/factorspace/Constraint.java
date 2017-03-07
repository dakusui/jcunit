package com.github.dakusui.jcunit8.model.factorspace;

import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.List;
import java.util.function.Predicate;

public interface Constraint<T> extends Predicate<T> {
  boolean test(T testObject);

  List<String> involvedKeys();

  interface ForTuple extends Predicate<Tuple> {
  }
}

package com.github.dakusui.jcunit8.factorspace;

import com.github.dakusui.jcunit8.core.tuples.Tuple;
import com.github.dakusui.jcunit8.utils.InternalUtils;

import java.util.List;
import java.util.function.Predicate;

import static com.github.dakusui.jcunit8.utils.InternalUtils.project;
import static java.util.Arrays.asList;

public interface Constraint extends TuplePredicate {
  static Constraint create(String name, Predicate<Tuple> predicate, List<String> args) {
    return new Constraint() {
      @Override
      public String getName() {
        return name;
      }

      @Override
      public boolean test(Tuple tuple) {
        return predicate.test(project(args, tuple));
      }

      @Override
      public List<String> involvedKeys() {
        return args;
      }

      @Override
      public String toString() {
        return String.format("%s:%s", InternalUtils.className(predicate.getClass()), args);
      }
    };
  }

  static Constraint create(String name, Predicate<Tuple> predicate, String... args) {
    return create(name, predicate, asList(args));
  }
}

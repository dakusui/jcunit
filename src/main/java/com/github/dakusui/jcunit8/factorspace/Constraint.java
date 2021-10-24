package com.github.dakusui.jcunit8.factorspace;

import com.github.dakusui.jcunit.core.tuples.KeyValuePairs;
import com.github.dakusui.jcunit8.core.Utils;

import java.util.List;
import java.util.function.Predicate;

import static com.github.dakusui.jcunit8.core.Utils.project;
import static java.util.Arrays.asList;

public interface Constraint extends TuplePredicate {
  static Constraint create(String name, Predicate<KeyValuePairs> predicate, List<String> args) {
    return new Constraint() {
      @Override
      public String getName() {
        return name;
      }

      @Override
      public boolean test(KeyValuePairs tuple) {
        return predicate.test(project(args, tuple));
      }

      @Override
      public List<String> involvedKeys() {
        return args;
      }

      @Override
      public String toString() {
        return String.format("%s:%s", Utils.className(predicate.getClass()), args);
      }
    };
  }

  static Constraint create(String name, Predicate<KeyValuePairs> predicate, String... args) {
    return create(name, predicate, asList(args));
  }
}

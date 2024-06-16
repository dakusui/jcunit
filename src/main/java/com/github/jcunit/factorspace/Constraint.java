package com.github.jcunit.factorspace;

import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.utils.InternalUtils;

import java.util.List;
import java.util.function.Predicate;

import static com.github.jcunit.utils.InternalUtils.project;
import static java.util.Arrays.asList;

public interface Constraint extends TuplePredicate {
  boolean isExplicit();
  static Constraint create(String name, Predicate<Tuple> predicate, List<String> args) {
    return create(name, false, args, predicate);
  }

  static Constraint create(String name, final boolean explicit, List<String> args, Predicate<Tuple> predicate) {
    return new Constraint() {
      @Override
      public boolean isExplicit() {
        return explicit;
      }

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

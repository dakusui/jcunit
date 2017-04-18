package com.github.dakusui.jcunit8.factorspace;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.core.Utils;

import java.util.List;
import java.util.function.Predicate;

import static com.github.dakusui.jcunit8.core.Utils.project;
import static java.util.Arrays.asList;

public interface Constraint extends TestPredicate {
  static Constraint fromCondition(TestPredicate testPredicate) {
    return new Constraint() {
      @Override
      public boolean test(Tuple tuple) {
        return testPredicate.test(tuple);
      }

      @Override
      public List<String> involvedKeys() {
        return testPredicate.involvedKeys();
      }

      @Override
      public String toString() {
        return testPredicate.toString();
      }
    };
  }

  static Constraint create(Predicate<Tuple> predicate, List<String> args) {
    return new Constraint() {
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
        return String.format("%s:%s", Utils.className(predicate.getClass()), args);
      }
    };
  }

  static Constraint create(Predicate<Tuple> predicate, String... args) {
    return create(predicate, asList(args));
  }
}

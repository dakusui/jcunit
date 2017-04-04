package com.github.dakusui.jcunit8.factorspace;

import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.List;

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
    };
  }
}

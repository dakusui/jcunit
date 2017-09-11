package com.github.dakusui.jcunit8.factorspace;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.core.Utils;

import java.util.List;
import java.util.function.Predicate;

public interface TestPredicate extends Predicate<Tuple> {
  String getName();

  boolean test(Tuple tuple);

  List<String> involvedKeys();

  static String toString(TestPredicate predicate) {
    return String.format(
        "%s:%s",
        Utils.className(predicate.getClass()), predicate.involvedKeys()
    );
  }

  static TestPredicate of(String name, List<String> involvedKeys, Predicate<Tuple> predicate) {
    return new TestPredicate() {
      @Override
      public String getName() {
        return name;
      }

      @Override
      public boolean test(Tuple tuple) {
        return predicate.test(tuple);
      }

      @Override
      public List<String> involvedKeys() {
        return involvedKeys;
      }
    };
  }
}

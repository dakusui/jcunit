package com.github.jcunit.factorspace;

import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.utils.InternalUtils;

import java.util.List;
import java.util.function.Predicate;

public interface TuplePredicate extends Predicate<Tuple> {
  String getName();

  boolean test(Tuple tuple);

  List<String> involvedKeys();

  static String toString(TuplePredicate predicate) {
    return String.format(
        "%s:%s",
        InternalUtils.className(predicate.getClass()), predicate.involvedKeys()
    );
  }

  static TuplePredicate of(String name, List<String> involvedKeys, Predicate<Tuple> predicate) {
    return new TuplePredicate() {
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

      @Override
      public String toString() {
        return name + involvedKeys();
      }
    };
  }
}

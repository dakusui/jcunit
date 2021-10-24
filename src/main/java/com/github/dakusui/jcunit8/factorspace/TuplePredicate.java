package com.github.dakusui.jcunit8.factorspace;

import com.github.dakusui.jcunit.core.tuples.KeyValuePairs;
import com.github.dakusui.jcunit8.core.Utils;

import java.util.List;
import java.util.function.Predicate;

public interface TuplePredicate extends Predicate<KeyValuePairs> {
  String getName();

  boolean test(KeyValuePairs tuple);

  List<String> involvedKeys();

  static String toString(TuplePredicate predicate) {
    return String.format(
        "%s:%s",
        Utils.className(predicate.getClass()), predicate.involvedKeys()
    );
  }

  static TuplePredicate of(String name, List<String> involvedKeys, Predicate<KeyValuePairs> predicate) {
    return new TuplePredicate() {
      @Override
      public String getName() {
        return name;
      }

      @Override
      public boolean test(KeyValuePairs tuple) {
        return predicate.test(tuple);
      }

      @Override
      public List<String> involvedKeys() {
        return involvedKeys;
      }
    };
  }
}

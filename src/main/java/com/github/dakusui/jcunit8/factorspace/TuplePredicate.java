package com.github.dakusui.jcunit8.factorspace;

import com.github.dakusui.jcunit.core.tuples.AArray;
import com.github.dakusui.jcunit8.core.Utils;

import java.util.List;
import java.util.function.Predicate;

public interface TuplePredicate extends Predicate<AArray> {
  String getName();

  boolean test(AArray tuple);

  List<String> involvedKeys();

  static String toString(TuplePredicate predicate) {
    return String.format(
        "%s:%s",
        Utils.className(predicate.getClass()), predicate.involvedKeys()
    );
  }

  static TuplePredicate of(String name, List<String> involvedKeys, Predicate<AArray> predicate) {
    return new TuplePredicate() {
      @Override
      public String getName() {
        return name;
      }

      @Override
      public boolean test(AArray tuple) {
        return predicate.test(tuple);
      }

      @Override
      public List<String> involvedKeys() {
        return involvedKeys;
      }
    };
  }
}

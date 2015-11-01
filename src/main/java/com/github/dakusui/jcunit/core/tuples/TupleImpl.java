package com.github.dakusui.jcunit.core.tuples;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;

import java.util.TreeMap;

// TODO: Comparison between LinkedHashMap and TreeMap
// Need to compare behavior change between LinkedHashMap and TreeMap
// Observation shows LinkedHashMap generates better (smaller) test suite.
// For now we don't change it for compatibility's sake.
public class TupleImpl extends /* LinkedHashMap<String, Object>*/ TreeMap<String, Object> implements Tuple {
  public Tuple cloneTuple() {
    return (Tuple) super.clone();
  }

  @Override public boolean isSubtupleOf(Tuple another) {
    return isSubtupleOf(this, another);
  }

  static boolean isSubtupleOf(Tuple a, Tuple b) {
    Checks.checknotnull(a);
    Checks.checknotnull(b);
    if (!b.keySet().containsAll(a.keySet())) {
      return false;
    }
    for (String k : a.keySet()) {
      if (!Utils.eq(a.get(k), b.get(k))) {
        return false;
      }
    }
    return true;

  }
}

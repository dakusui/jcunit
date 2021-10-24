package com.github.dakusui.jcunit.core.tuples;

import com.github.dakusui.combinatoradix.Combinator;
import com.github.dakusui.jcunit.core.utils.Checks;

import java.util.*;

import static java.util.Objects.requireNonNull;

public enum TupleUtils {
  ;

  public static Set<KeyValuePairs> subtuplesOf(
      KeyValuePairs tuple, int strength) {
    Checks.checknotnull(tuple);
    Checks.checkcond(strength >= 0 && strength <= tuple.size());
    Set<KeyValuePairs> ret = new LinkedHashSet<>();
    Combinator<String> c = new Combinator<>(
        new LinkedList<>(tuple.keySet()), strength);
    for (List<String> keys : c) {
      KeyValuePairs cur = new KeyValuePairs.Impl();
      for (String k : keys) {
        cur.put(k, tuple.get(k));
      }
      ret.add(cur);
    }
    return ret;
  }

  public static Set<KeyValuePairs> subtuplesOf(KeyValuePairs tuple) {
    Checks.checknotnull(tuple);
    Set<KeyValuePairs> ret = new LinkedHashSet<>();
    int sz = tuple.size();
    for (int i = 0; i <= sz; i++) {
      ret.addAll(subtuplesOf(tuple, sz - i));
    }
    return ret;
  }

  public static Set<KeyValuePairs> connectingSubtuplesOf(KeyValuePairs lhs, KeyValuePairs rhs, int strength) {
    Checks.checkcond(strength >= 0);
    Checks.checkcond(strength <= lhs.size() + rhs.size());
    Checks.checkcond(Collections.disjoint(lhs.keySet(), rhs.keySet()));
    Set<KeyValuePairs> ret = new LinkedHashSet<>();
    for (int i = 1; i < strength; i++) {
      if (i > lhs.size())
        break;
      if (i < strength - rhs.size())
        continue;
      for (KeyValuePairs eachFromLhs : subtuplesOf(lhs, i))
        for (KeyValuePairs eachFromRhs : subtuplesOf(rhs, strength - i))
          ret.add(KeyValuePairs.builder().putAll(eachFromLhs).putAll(eachFromRhs).buildTuple());
    }
    return ret;
  }

  public static KeyValuePairs project(KeyValuePairs tuple, List<String> factorNames) {
    KeyValuePairs.Builder builder = new KeyValuePairs.Builder();
    factorNames.stream().filter(tuple::containsKey).forEach(each -> builder.put(each, tuple.get(each)));
    return builder.buildTuple();
  }

  public static KeyValuePairs copy(KeyValuePairs tuple) {
    return new KeyValuePairs.Builder().putAll(requireNonNull(tuple)).buildTuple();
  }
}

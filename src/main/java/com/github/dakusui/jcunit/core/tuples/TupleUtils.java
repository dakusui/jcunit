package com.github.dakusui.jcunit.core.tuples;

import com.github.dakusui.combinatoradix.Combinator;
import com.github.dakusui.jcunit.core.utils.Checks;

import java.util.*;

import static java.util.Objects.requireNonNull;

public enum TupleUtils {
  ;

  public static Set<AArray> subtuplesOf(
      AArray tuple, int strength) {
    Checks.checknotnull(tuple);
    Checks.checkcond(strength >= 0 && strength <= tuple.size());
    Set<AArray> ret = new LinkedHashSet<>();
    Combinator<String> c = new Combinator<>(
        new LinkedList<>(tuple.keySet()), strength);
    for (List<String> keys : c) {
      AArray cur = new AArray.Impl();
      for (String k : keys) {
        cur.put(k, tuple.get(k));
      }
      ret.add(cur);
    }
    return ret;
  }

  public static Set<AArray> subtuplesOf(AArray tuple) {
    Checks.checknotnull(tuple);
    Set<AArray> ret = new LinkedHashSet<>();
    int sz = tuple.size();
    for (int i = 0; i <= sz; i++) {
      ret.addAll(subtuplesOf(tuple, sz - i));
    }
    return ret;
  }

  public static Set<AArray> connectingSubtuplesOf(AArray lhs, AArray rhs, int strength) {
    Checks.checkcond(strength >= 0);
    Checks.checkcond(strength <= lhs.size() + rhs.size());
    Checks.checkcond(Collections.disjoint(lhs.keySet(), rhs.keySet()));
    Set<AArray> ret = new LinkedHashSet<>();
    for (int i = 1; i < strength; i++) {
      if (i > lhs.size())
        break;
      if (i < strength - rhs.size())
        continue;
      for (AArray eachFromLhs : subtuplesOf(lhs, i))
        for (AArray eachFromRhs : subtuplesOf(rhs, strength - i))
          ret.add(AArray.builder().putAll(eachFromLhs).putAll(eachFromRhs).build());
    }
    return ret;
  }

  public static AArray project(AArray tuple, List<String> factorNames) {
    AArray.Builder builder = new AArray.Builder();
    factorNames.stream().filter(tuple::containsKey).forEach(each -> builder.put(each, tuple.get(each)));
    return builder.build();
  }

  public static AArray copy(AArray tuple) {
    return new AArray.Builder().putAll(requireNonNull(tuple)).build();
  }
}

package com.github.dakusui.jcunit.core.tuples;

import com.github.dakusui.combinatoradix.Combinator;
import com.github.dakusui.jcunit.core.utils.Checks;

import java.util.*;

import static java.util.Objects.requireNonNull;

public enum TupleUtils {
  ;

  public static Set<Tuple> subtuplesOf(
      Tuple tuple, int strength) {
    Checks.checknotnull(tuple);
    Checks.checkcond(strength >= 0 && strength <= tuple.size());
    Set<Tuple> ret = new LinkedHashSet<>();
    Combinator<String> c = new Combinator<>(
        new LinkedList<>(tuple.keySet()), strength);
    for (List<String> keys : c) {
      Tuple cur = new Tuple.Impl();
      for (String k : keys) {
        cur.put(k, tuple.get(k));
      }
      ret.add(cur);
    }
    return ret;
  }

  public static Set<Tuple> subtuplesOf(Tuple tuple) {
    Checks.checknotnull(tuple);
    Set<Tuple> ret = new LinkedHashSet<>();
    int sz = tuple.size();
    for (int i = 0; i <= sz; i++) {
      ret.addAll(subtuplesOf(tuple, sz - i));
    }
    return ret;
  }

  public static Set<Tuple> connectingSubtuplesOf(Tuple lhs, Tuple rhs, int strength) {
    Checks.checkcond(strength >= 0);
    Checks.checkcond(strength <= lhs.size() + rhs.size());
    Checks.checkcond(Collections.disjoint(lhs.keySet(), rhs.keySet()));
    Set<Tuple> ret = new LinkedHashSet<>();
    for (int i = 1; i < strength; i++) {
      if (i > lhs.size())
        break;
      if (i < strength - rhs.size())
        continue;
      for (Tuple eachFromLhs : subtuplesOf(lhs, i))
        for (Tuple eachFromRhs : subtuplesOf(rhs, strength - i))
          ret.add(Tuple.builder().putAll(eachFromLhs).putAll(eachFromRhs).build());
    }
    return ret;
  }

  public static Tuple project(Tuple tuple, List<String> factorNames) {
    Tuple.Builder builder = new Tuple.Builder();
    factorNames.stream().filter(tuple::containsKey).forEach(each -> builder.put(each, tuple.get(each)));
    return builder.build();
  }

  public static Tuple copy(Tuple tuple) {
    return new Tuple.Builder().putAll(requireNonNull(tuple)).build();
  }
}

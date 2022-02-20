package com.github.dakusui.jcunit.core.tuples;

import com.github.dakusui.combinatoradix.Combinator;
import com.github.dakusui.jcunit.core.utils.Checks;

import java.util.*;

import static java.util.Objects.requireNonNull;

public enum TupleUtils {
  ;

  public static Set<Aarray> subtuplesOf(
      Aarray tuple, int strength) {
    Checks.checknotnull(tuple);
    Checks.checkcond(strength >= 0 && strength <= tuple.size());
    Set<Aarray> ret = new LinkedHashSet<>();
    Combinator<String> c = new Combinator<>(
        new LinkedList<>(tuple.keySet()), strength);
    for (List<String> keys : c) {
      Aarray cur = new Aarray.Impl();
      for (String k : keys) {
        cur.put(k, tuple.get(k));
      }
      ret.add(cur);
    }
    return ret;
  }

  public static Set<Aarray> subtuplesOf(Aarray tuple) {
    Checks.checknotnull(tuple);
    Set<Aarray> ret = new LinkedHashSet<>();
    int sz = tuple.size();
    for (int i = 0; i <= sz; i++) {
      ret.addAll(subtuplesOf(tuple, sz - i));
    }
    return ret;
  }

  public static Set<Aarray> connectingSubtuplesOf(Aarray lhs, Aarray rhs, int strength) {
    Checks.checkcond(strength >= 0);
    Checks.checkcond(strength <= lhs.size() + rhs.size());
    Checks.checkcond(Collections.disjoint(lhs.keySet(), rhs.keySet()));
    Set<Aarray> ret = new LinkedHashSet<>();
    for (int i = 1; i < strength; i++) {
      if (i > lhs.size())
        break;
      if (i < strength - rhs.size())
        continue;
      for (Aarray eachFromLhs : subtuplesOf(lhs, i))
        for (Aarray eachFromRhs : subtuplesOf(rhs, strength - i))
          ret.add(Aarray.builder().putAll(eachFromLhs).putAll(eachFromRhs).build());
    }
    return ret;
  }

  public static Aarray project(Aarray tuple, List<String> factorNames) {
    Aarray.Builder builder = new Aarray.Builder();
    factorNames.stream().filter(tuple::containsKey).forEach(each -> builder.put(each, tuple.get(each)));
    return builder.build();
  }

  public static Aarray copy(Aarray tuple) {
    return new Aarray.Builder().putAll(requireNonNull(tuple)).build();
  }
}

package com.github.dakusui.jcunitx.utils;

import com.github.dakusui.combinatoradix.Combinator;
import com.github.dakusui.jcunitx.core.AArray;

import java.util.*;
import java.util.function.Predicate;

import static com.github.dakusui.pcond.Assertions.that;
import static com.github.dakusui.pcond.core.refl.MethodQuery.classMethod;
import static com.github.dakusui.pcond.functions.Functions.parameter;
import static com.github.dakusui.pcond.functions.Predicates.*;
import static java.util.Objects.requireNonNull;

public enum TupleUtils {
  ;

  public static Set<AArray> subtuplesOf(AArray aarray, int strength) {
    Checks.checknotnull(aarray);
    Checks.checkcond(strength >= 0 && strength <= aarray.size());
    Set<AArray> ret = new LinkedHashSet<>();
    Combinator<String> c = new Combinator<>(
        new LinkedList<>(aarray.keySet()), strength);
    for (List<String> keys : c) {
      AArray cur = new AArray.Impl();
      for (String k : keys) {
        cur.put(k, aarray.get(k));
      }
      ret.add(cur);
    }
    return ret;
  }

  public static Set<AArray> subtuplesOf(AArray aarray) {
    Checks.checknotnull(aarray);
    Set<AArray> ret = new LinkedHashSet<>();
    int sz = aarray.size();
    for (int i = 0; i <= sz; i++) {
      ret.addAll(subtuplesOf(aarray, sz - i));
    }
    return ret;
  }

  public static Set<AArray> connectingSubtuplesOf(AArray lhs, AArray rhs, int strength) {
    assert that(strength, allOf(
        greaterThanOrEqualTo(0),
        lessThanOrEqualTo(lhs.size() + rhs.size())));
    assert that(lhs.keySet(), disjointWith(rhs.keySet()));
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


  private static Predicate<Set<String>> disjointWith(Set<String> rhs) {
    return callp(classMethod(Collections.class, "disjoint", parameter(), rhs));
  }
}

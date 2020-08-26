package com.github.dakusui.jcunit.core.tuples;

import com.github.dakusui.combinatoradix.Combinator;
import com.github.dakusui.jcunit.core.utils.Checks;

import java.util.*;
import java.util.function.Function;

import static com.github.dakusui.jcunit8.core.Utils.memoize;
import static java.util.Objects.requireNonNull;

public enum TupleUtils {
  ;
  private static final Function<Integer, Function<Set<String>, List<List<String>>>> KEY_SET_COMBINATIONS = keySetCombinations();

  public static Set<Tuple> subtuplesOf(Tuple tuple, int strength) {
    assert tuple != null;
    assert strength >= 0;
    assert strength <= tuple.size();
    return subtuplesOf(tuple, KEY_SET_COMBINATIONS.apply(strength).apply(tuple.keySet()));
  }

  private static Function<Integer, Function<Set<String>, List<List<String>>>> keySetCombinations() {
    return memoize(strength -> memoize(keySet -> keySetCombinations(strength, keySet)));
  }

  private static List<List<String>> keySetCombinations(int strength, Set<String> keySet) {
    List<List<String>> ret = new LinkedList<>();
    for (List<String> each : new Combinator<>(new LinkedList<>(keySet), strength))
      ret.add(each);
    return ret;
  }

  private static Set<Tuple> subtuplesOf(Tuple tuple, Iterable<List<String>> c) {
    Set<Tuple> ret = new LinkedHashSet<>();
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

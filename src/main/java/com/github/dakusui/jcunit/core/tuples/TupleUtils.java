package com.github.dakusui.jcunit.core.tuples;

import com.github.dakusui.combinatoradix.Combinator;
import com.github.dakusui.jcunit.core.utils.Checks;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

  public static Tuple project(Tuple tuple, List<String> factorNames) {
    Tuple.Builder builder = new Tuple.Builder();
    factorNames.forEach(each -> builder.put(each, tuple.get(each)));
    return builder.build();
  }

  public static Tuple copy(Tuple tuple) {
    return new Tuple.Builder().putAll(requireNonNull(tuple)).build();
  }
}

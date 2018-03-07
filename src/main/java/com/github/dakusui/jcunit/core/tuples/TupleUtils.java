package com.github.dakusui.jcunit.core.tuples;

import com.github.dakusui.combinatoradix.Combinator;
import com.github.dakusui.jcunit.core.utils.Checks;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public enum TupleUtils {
  ;

  public static Set<Tuple> subtuplesOf(
      Tuple tuple, int strength) {
    return subtuplesOf(LinkedHashSet::new, tuple, strength);
  }

  public static Set<Tuple> subtuplesOf(
      Supplier<Set<Tuple>> setSupplier,
      Tuple tuple, int strength) {
    Checks.checknotnull(tuple);
    Checks.checkcond(strength >= 0 && strength <= tuple.size());
    Set<Tuple> ret = setSupplier.get();
    for (List<String> keys : new Combinator<>(new ArrayList<>(tuple.keySet()), strength)) {
      ret.add(TupleUtils.project(tuple, keys));
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
    return connectingSubtuplesOf(LinkedHashSet::new, lhs, rhs, strength);
  }

  public static Set<Tuple> connectingSubtuplesOf(Supplier<Set<Tuple>> setSupplier, Tuple lhs, Tuple rhs, int strength) {
    Checks.checkcond(strength >= 0);
    Checks.checkcond(strength <= lhs.size() + rhs.size());
    //    Checks.checkcond(Collections.disjoint(lhs.keySet(), rhs.keySet()));
    Set<Tuple> ret = setSupplier.get();
    for (int i = 1; i < strength; i++) {
      if (i > lhs.size())
        break;
      if (i < strength - rhs.size())
        continue;
      for (Tuple eachFromLhs : subtuplesOf(setSupplier, lhs, i))
        for (Tuple eachFromRhs : subtuplesOf(setSupplier, rhs, strength - i))
          ret.add(Tuple.builder().putAll(eachFromLhs).putAll(eachFromRhs).build());
    }
    return ret;
  }

  public static Tuple project(Tuple tuple, List<String> factorNames) {
    Tuple.Builder builder = Tuple.builder();
    factorNames.stream().filter(tuple::containsKey).forEach(each -> builder.put(each, tuple.get(each)));
    return builder.build();
  }

  public static Tuple copy(Tuple tuple) {
    return Tuple.builder().putAll(requireNonNull(tuple)).build();
  }

  public static Tuple connect(Tuple tuple1, Tuple tuple2) {
    return Tuple.builder().putAll(tuple1).putAll(tuple2).build();
  }

  public static Set<Tuple> covered(Set<Tuple> tuples, Tuple t, int doi) {
    Set<Tuple> ret = new LinkedHashSet<>();
    TupleUtils.subtuplesOf(t, doi).forEach(
        each -> {
          if (ret.contains(each))
            ret.add(each);
        }
    );
    return ret;
  }
}

package com.github.dakusui.jcunit.generators.ipo2;

import com.github.dakusui.enumerator.Combinator;
import com.github.dakusui.enumerator.Enumerator;
import com.github.dakusui.enumerator.tuple.AttrValue;
import com.github.dakusui.enumerator.tuple.CartesianEnumerator;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.ValueTuple;

import java.util.*;

/**
 * Created by hiroshi on 6/30/14.
 */
public class LeftTuples {
  private final Set<ValueTuple<String, Object>> tuples;
  private final String                          factor;
  private final int                             strength;

  LeftTuples(LinkedHashMap<String, Object[]> domains, String factor,
      int strength) {
    Utils.checknotnull(domains);
    Utils.checknotnull(factor);
    this.factor = factor;
    this.strength = strength;
    this.tuples = init(IPO2Utils.headMap(domains, factor), factor,
        domains.get(factor));
  }

  protected Set<ValueTuple<String, Object>> init(
      LinkedHashMap<String, Object[]> domains, String factorName,
      Object[] factorLevels) {
    Set<ValueTuple<String, Object>> ret = new HashSet<ValueTuple<String, Object>>();
    List<AttrValue<String, Object>> work = IPO2Utils.map2list(domains);
    for (Object l : factorLevels) {
      work.add(new AttrValue<String, Object>(factorName, l));
    }
    Enumerator<String> combinator = new Combinator<String>(new LinkedList<String>(domains.keySet()), this.strength - 1);

    for (List<String> keys : combinator) {
      List<AttrValue<String, Object>> cur = new LinkedList<AttrValue<String, Object>>();
      for (String k : keys) {
        for (Object o : domains.get(k)) {
          cur.add(new AttrValue<String, Object>(k, o));
        }
      }
      for (Object o : factorLevels) {
        cur.add(new AttrValue<String, Object>(factorName, o));
      }
      CartesianEnumerator<String, Object> ce = new CartesianEnumerator<String, Object>(
          cur);
      for (
          List<AttrValue<String, Object>> attrValues
          : ce) {
        ret.add(IPO2Utils.list2tuple(attrValues));
      }
    }
    return ret;
  }

  public void add(ValueTuple<String, Object> c) {
    this.add(c);
  }

  void removeAll(Set<ValueTuple<String, Object>> tuples) {
    this.tuples.removeAll(tuples);
  }

  public boolean isEmpty() {
    return this.tuples.isEmpty();
  }

  public Set<ValueTuple<String, Object>> yetToCover() {
    return this.tuples;
  }

  public Set<ValueTuple<String, Object>> coveredBy(
      ValueTuple<String, Object> tuple) {
    Set<ValueTuple<String, Object>> ret = new LinkedHashSet<ValueTuple<String, Object>>();
    Set<ValueTuple<String, Object>> possibleTuples = IPO2.tuplesCoveredBy(tuple,
        this.strength);
    for (ValueTuple<String, Object> c : possibleTuples) {
      if (this.tuples.contains(c)) {
        ret.add(c);
      }
    }
    return ret;
  }

  @Override
  public int hashCode() {
    return this.factor.hashCode();
  }

  @Override
  public boolean equals(Object anotherObject) {
    if (!(anotherObject instanceof LeftTuples)) {
      return false;
    }
    LeftTuples another = (LeftTuples) anotherObject;
    if (!this.factor.equals(((LeftTuples) anotherObject).factor)) {
      return false;
    }
    return this.tuples.equals(another.tuples);
  }

  public void addAll(LinkedHashSet<ValueTuple<String, Object>> leftOver) {
    this.tuples.addAll(leftOver);
  }
}

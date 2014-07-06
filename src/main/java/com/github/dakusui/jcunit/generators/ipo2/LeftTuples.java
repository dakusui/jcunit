package com.github.dakusui.jcunit.generators.ipo2;

import com.github.dakusui.jcunit.core.Utils;

import java.util.*;

public class LeftTuples {
  private final Set<Tuple> tuples;
  private final String     factor;
  private final int        strength;

  LeftTuples(Factors factors, Factor factor,
      int strength) {
    Utils.checknotnull(factors);
    Utils.checknotnull(factor);
    Utils.checkcond(!factors.contains(factor), String
        .format("factors(%s) mustn't contain '%s'", factors.getFactorNames(),
            factor.name));
    this.factor = factor.name;
    this.strength = strength;
    this.tuples = init(factors, factor);
  }

  protected Set<Tuple> init(
      Factors factors, Factor factor) {
    Set<Tuple> ret = new HashSet<Tuple>();
    for (Tuple t : factors.generateAllPossibleTuples(this.strength - 1)) {
      for (Object l : factor) {
        Tuple tt = t.clone();
        tt.put(factor.name, l);
        ret.add(tt);
      }
    }
    return ret;
  }

  public void add(Tuple tuple) {
    this.tuples.add(tuple);
  }

  void removeAll(Set<Tuple> tuples) {
    this.tuples.removeAll(tuples);
  }

  public boolean isEmpty() {
    return this.tuples.isEmpty();
  }

  public List<Tuple> leftTuples() {
    return new LinkedList<Tuple>(this.tuples);
  }

  public Set<Tuple> coveredBy(
      Tuple tuple) {
    Set<Tuple> ret = new LinkedHashSet<Tuple>();
    Set<Tuple> possibleTuples = IPO2.tuplesCoveredBy(tuple,
        this.strength);
    for (Tuple c : possibleTuples) {
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
    return this.factor.equals(((LeftTuples) anotherObject).factor)
        && this.tuples.equals(another.tuples);
  }

  public void addAll(Set<Tuple> leftOver) {
    this.tuples.addAll(leftOver);
  }

  public boolean contains(Tuple tuple) {
    return this.tuples.contains(tuple);
  }
}

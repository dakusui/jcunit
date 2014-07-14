package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;

import java.util.*;

public class Tuples {
  private final Set<Tuple> tuples;
  private final String     factorName;
  private final int        strength;

  public Tuples(Factors factors, Factor factorName,
      int strength) {
    Utils.checknotnull(factors);
    Utils.checknotnull(factorName);
    Utils.checkcond(!factors.contains(factorName), String
        .format("factors(%s) mustn't contain '%s'", factors.getFactorNames(),
            factorName.name));
    this.factorName = factorName.name;
    this.strength = strength;
    this.tuples = init(factors, factorName);
  }

  private Tuples(Tuples tuples) {
    this.tuples = Collections.unmodifiableSet(tuples.tuples);
    this.factorName = tuples.factorName;
    this.strength = tuples.strength;
  }

  protected Set<Tuple> init(
      Factors factors, Factor factor) {
    Set<Tuple> ret = new HashSet<Tuple>();
    for (Tuple t : factors.generateAllPossibleTuples(this.strength - 1)) {
      for (Object l : factor) {
        Tuple tt = t.cloneTuple();
        tt.put(factor.name, l);
        ret.add(tt);
      }
    }
    return ret;
  }

  public void add(Tuple tuple) {
    this.tuples.add(tuple);
  }

  public void removeAll(Set<Tuple> tuples) {
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
    Set<Tuple> possibleTuples = TupleUtils.subtuplesOf(tuple,
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
    return this.factorName.hashCode();
  }

  @Override
  public boolean equals(Object anotherObject) {
    if (!(anotherObject instanceof Tuples)) {
      return false;
    }
    Tuples another = (Tuples) anotherObject;
    return this.factorName.equals(((Tuples) anotherObject).factorName)
        && this.tuples.equals(another.tuples);
  }

  public void addAll(Set<Tuple> leftOver) {
    this.tuples.addAll(leftOver);
  }

  public boolean contains(Tuple tuple) {
    return this.tuples.contains(tuple);
  }

  public Tuples unmodifiableVersion() {
    return new Tuples(this);
  }
}

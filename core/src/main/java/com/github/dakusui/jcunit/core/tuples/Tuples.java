package com.github.dakusui.jcunit.core.tuples;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;

import java.util.*;

public class Tuples {
  private final Set<Tuple> tuples;
  private final int        strength;

  public Tuples(Factors factors, Factor factor,
      int strength) {
    Checks.checknotnull(factors);
    Checks.checknotnull(factor);
    Checks.checkcond(!factors.contains(factor),
        "factors(%s) mustn't contain '%s'", factors.getFactorNames(),
        factor.name);
    this.strength = strength;
    this.tuples = init(factors, factor);
  }

  private Tuples(Tuples tuples) {
    this.tuples = Collections.unmodifiableSet(tuples.tuples);
    this.strength = tuples.strength;
  }

  protected Set<Tuple> init(Factors factors, Factor factor) {
    Set<Tuple> ret = new LinkedHashSet<Tuple>();
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

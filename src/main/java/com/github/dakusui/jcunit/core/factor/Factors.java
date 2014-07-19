package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.enumerator.Combinator;
import com.github.dakusui.enumerator.tuple.AttrValue;
import com.github.dakusui.enumerator.tuple.CartesianEnumerator;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleImpl;
import com.github.dakusui.jcunit.core.Utils;

import java.util.*;

public class Factors implements Iterable<Factor> {
  private final List<Factor>        factors;
  private final Map<String, Factor> factorMap;

  public Factors(List<Factor> factors) {
    Utils.checknotnull(factors);
    this.factors = Collections.unmodifiableList(factors);
    Map<String, Factor> factorMap = new HashMap<String, Factor>();
    for (Factor f : factors) {
      Utils.checkcond(!factorMap.containsKey(f.name), "There are more than one factors whose names are '%s'.", f.name);
      factorMap.put(f.name, f);
    }
    this.factorMap = factorMap;
  }

  /**
   * Returns a new {@code Factors} object adding a given new {@code Factor} object.
   */
  public Factors add(Factor factor) {
    List<Factor> factors = new LinkedList<Factor>(this.factors);
    factors.add(factor);
    return new Factors(factors);
  }

  @Override
  public Iterator<Factor> iterator() {
    return this.factors.iterator();
  }

  public List<String> getFactorNames() {
    List<String> ret = new ArrayList<String>(factors.size());
    for (Factor f : this.factors) {
      ret.add(f.name);
    }
    return ret;
  }

  public int size() {
    return this.factors.size();
  }

  public Factor get(String factorName) {
    Utils.checknotnull(factorName);
    return this.factorMap.get(factorName);
  }

  public Factor get(int index) {
    Utils.checkcond(index > 0);
    Utils.checkcond(index < this.factors.size());
    return this.factors.get(index);
  }

  public boolean has(String factorName) {
    return this.factorMap.containsKey(factorName);
  }

  public String nextKey(String factorName) {
    Utils.checknotnull(factorName);
    Utils.checkcond(this.factorMap.containsKey(factorName));
    Factor f = get(factorName);
    int i = this.factors.indexOf(f);
    Utils.checkcond(i < this.factors.size() - 1,
        "'%s' is the last factor name.", factorName);
    Factor g = get(i + 1);
    return g.name;
  }

  public boolean isLastKey(String key) {
    return Utils.eq(key, this.factors.get(this.factors.size() - 1).name);
  }

  public Factors head(String to) {
    Utils.checknotnull(to);
    Utils.checkcond(has(to));
    List<Factor> factors = new LinkedList<Factor>();
    for (Factor f : this.factors) {
      if (to.equals(f.name)) {
        return new Factors(factors);
      }
      factors.add(f);
    }
    throw new RuntimeException("Something went wrong.");
  }

  public Factors tail(String from) {
    Utils.checknotnull(from);
    Utils.checkcond(has(from));
    List<Factor> factors = new LinkedList<Factor>();
    boolean found = false;
    for (Factor f : this.factors) {
      if (f.name.equals(from)) {
        found = true;
      }
      if (found) {
        factors.add(f);
      }
    }
    return new Factors(factors);
  }

  public List<Tuple> generateAllPossibleTuples(
      int strength) {
    List<Tuple> ret = new LinkedList<Tuple>();
    Combinator<String> c = new Combinator<String>(this.getFactorNames(),
        strength);
    for (List<String> factorNames : c) {
      List<AttrValue<String, Object>> attrValues = new LinkedList<AttrValue<String, Object>>();
      for (String factorName : factorNames) {
        attrValues.addAll(get(factorName).asAttrValues());
      }
      CartesianEnumerator<String, Object> ce = new CartesianEnumerator<String, Object>(
          attrValues);
      for (List<AttrValue<String, Object>> t : ce) {
        Tuple tuple = new TupleImpl();
        for (AttrValue<String, Object> attrValue : t) {
          tuple.put(attrValue.attr(), attrValue.value());
        }
        ret.add(tuple);
      }
    }
    return ret;
  }

  /**
   * Creates a new tuple which has values for all the factors defined in this object
   * using values given by {@code tuple}.
   * For factors that do not appear in {@code tuple}, {@code defaultValue} will
   * be used.
   * <p/>
   * The {@code tuple} must not contain any keys which are not defined in this object.
   * <p/>
   * The object {@code tuple} will remain unchanged after a call of this method.
   */
  public Tuple createTupleFrom(Tuple tuple, Object defaultValue) {
    Utils.checknotnull(tuple);
    for (String k : tuple.keySet()) {
      Utils.checkcond(this.factorMap.containsKey(k),"Undefined factor '%s' was found: defined keys (%s)", k,
              this.getFactorNames());
    }
    Tuple ret = tuple.cloneTuple();
    for (String k : getFactorNames()) {
      if (!ret.containsKey(k)) {
        ret.put(k, defaultValue);
      }
    }
    return ret;
  }

  public boolean contains(Factor factor) {
    return this.factors.contains(factor);
  }

  public static class Builder {
    private final List<Factor> factors = new LinkedList<Factor>();

    public Builder add(Factor f) {
      Utils.checknotnull(f);
      this.factors.add(f);
      return this;
    }

    public Factors build() {
      return new Factors(this.factors);
    }
  }
}

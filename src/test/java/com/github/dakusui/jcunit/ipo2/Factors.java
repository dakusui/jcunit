package com.github.dakusui.jcunit.ipo2;

import com.github.dakusui.enumerator.Combinator;
import com.github.dakusui.enumerator.tuple.AttrValue;
import com.github.dakusui.enumerator.tuple.CartesianEnumerator;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.ValueTuple;

import java.util.*;

/**
* Created by hiroshi on 7/3/14.
*/
public class Factors implements Iterable<Factor> {
  private final List<Factor>        factors;
  private final Map<String, Factor> factorMap;

  public Factors(List<Factor> factors) {
    Utils.checknotnull(factors);
    this.factors = Collections.unmodifiableList(factors);
    Map<String, Factor> factorMap = new HashMap<String, Factor>();
    for (Factor f : factors) {
      factorMap.put(f.name, f);
    }
    this.factorMap = factorMap;
  }

  public List<String> getFactorNames() {
    List<String> ret = new ArrayList<String>(factors.size());
    for (Factor f : this.factors) {
      ret.add(f.name);
    }
    return ret;
  }

  @Override public Iterator<Factor> iterator() {
    return this.factors.iterator();
  }

  public Factor get(String factorName) {
    Utils.checknotnull(factorName);
    return this.factorMap.get(factorName);
  }

  public boolean has(String factorName) {
    return this.factorMap.containsKey(factorName);
  }

  public List<ValueTuple<String, Object>> generateAllPossibleTuples(
      int strength) {
    List<ValueTuple<String, Object>> ret = new LinkedList<ValueTuple<String, Object>>();
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
        ValueTuple<String, Object> tuple = new ValueTuple<String, Object>();
        for (AttrValue<String, Object> attrValue : t) {
          tuple.put(attrValue.attr(), attrValue.value());
        }
        ret.add(tuple);
      }
    }
    return ret;
  }

  public Factors head(String to) {
    Utils.checknotnull(to);
    Utils.checkcond(has(to));
    List<Factor> factors = new LinkedList<Factor>();
    for (Factor f : this.factors) {
      factors.add(f);
      if (to.equals(f.name)) {
        return new Factors(factors);
      }
    }
    throw new RuntimeException("Something went wrong.");
  }

  public Factors tail(String from) {
    Utils.checknotnull(from);
    Utils.checkcond(has(from));
    List<Factor> factors = new LinkedList<Factor>();
    boolean found = false;
    for (Factor f : this.factors) {
      if (f.name.equals(from)) found = true;
      if (found) {
        factors.add(f);
      }
    }
    return new Factors(factors);
  }


  public LinkedHashMap<String, Object[]> toLinkedHashMap() {
    LinkedHashMap<String, Object[]> ret = new LinkedHashMap<String, Object[]>();
    for (String factorName : this.getFactorNames()) {
      ret.put(factorName, get(factorName).levels.toArray());
    }
    return ret;
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

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
    return this.factorMap.get(factorName);
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

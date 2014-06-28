package com.github.dakusui.jcunit.generators.ipo;

import com.github.dakusui.enumerator.Combinator;
import com.github.dakusui.enumerator.tuple.AttrValue;
import com.github.dakusui.enumerator.tuple.CartesianEnumerator;
import com.github.dakusui.jcunit.constraints.ConstraintManager;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.ValueTuple;

import java.util.*;

/**
 * Created by hiroshi on 14/06/28.
 */
public class IPO2 {
  private static final Object DontCare = new Object() {
    @Override
    public String toString() {
      return "D/C";
    }
  };

  static class LeftOvers {
    private final Set<ValueTuple<String, Object>> yetToCover;
    private final String                          factor;

    LeftOvers(SortedMap<String, Object[]> domains, String factor) {
      Utils.checknotnull(domains);
      Utils.checknotnull(factor);
      this.factor = factor;
      this.yetToCover = init(domains.subMap(domains.firstKey(), factor), factor, domains.get(factor));
    }

    protected Set<ValueTuple<String, Object>> init(SortedMap<String, Object[]> domains, String factor, Object[] values) {
      return null;
    }


    @Override
    public int hashCode() {
      return this.factor.hashCode();
    }

    @Override
    public boolean equals(Object anotherObject) {
      if (!(anotherObject instanceof LeftOvers)) {
        return false;
      }
      LeftOvers another = (LeftOvers) anotherObject;
      if (!this.factor.equals(((LeftOvers) anotherObject).factor)) {
        return false;
      }
      return this.yetToCover.equals(another.yetToCover);
    }

    void cover(Set<ValueTuple<String, Object>> tuples) {
      this.yetToCover.removeAll(tuples);
    }

    public boolean isEmpty() {
      return this.yetToCover.isEmpty();
    }

    public Set<ValueTuple<String, Object>> yetToCover() {
      return this.yetToCover;
    }
  }

  private final ConstraintManager                constraintManager;
  private final SortedMap
                    <String, Object[]>           domains;
  private final int                              strength;
  private       List<ValueTuple<String, Object>> result;
  private final Map<String, LeftOvers>           factorProcessors;

  public IPO2(SortedMap<String, Object[]> domains, int strength, ConstraintManager constraintManager) {
    Utils.checknotnull(domains);
    Utils.checkcond(strength >= 2);
    Utils.checkcond(domains.size() >= strength);
    this.domains = domains;
    this.strength = 2;
    this.result = null;
    this.constraintManager = constraintManager;
    this.factorProcessors = new HashMap<String, LeftOvers>();
  }

  public void ipo() {
    if (this.strength < this.domains.size()) {
      this.result = initialTestCases(this.domains.headMap(nthKey(this.strength, this.domains)));
    } else if (domains.size() == this.strength) {
      this.result = initialTestCases(this.domains);
      return;
    }

    for (String k : this.domains.tailMap(nthKey(strength, this.domains)).keySet()) {
      LeftOvers leftOvers = new LeftOvers(domains, k);
      hg(result, leftOvers, k, domains.get(k));
      if (leftOvers.isEmpty()) {
        continue;
      }
      vg(result, leftOvers, k, this.domains.headMap(k));
    }
  }

  public List<ValueTuple<String, Object>> getResult() {
    Utils.checkcond(this.result != null, "Execute ipo() method first");
    return Collections.unmodifiableList(this.result);
  }

  private List<ValueTuple<String, Object>> initialTestCases(SortedMap<String, Object[]> domains) {
    List<AttrValue<String, Object>> attrValues = new LinkedList<AttrValue<String, Object>>();
    for (String k : domains.keySet()) {
      for (Object v : domains.get(k)) {
        attrValues.add(new AttrValue<String, Object>(k, v));
      }
    }

    CartesianEnumerator<String, Object> ce = new CartesianEnumerator<String, Object>(attrValues);
    List<ValueTuple<String, Object>> ret = new ArrayList<ValueTuple<String, Object>>((int) ce.size());
    for (List<AttrValue<String, Object>> t : ce) {
      ValueTuple<String, Object> tuple = new ValueTuple<String, Object>();
      for (AttrValue<String, Object> a : t) {
        tuple.put(a.attr(), a.value());
      }
      ret.add(tuple);
    }
    return ret;
  }

  private void hg(List<ValueTuple<String, Object>> result, LeftOvers leftOvers, String k, Object[] values) {
    for (int i = 0; i < values.length; i++) {
      ValueTuple<String, Object> cand = result.get(i);
      cand.put(k, values[i]);
      try {
        if (i < result.size()) {
          cand.put(k, values[i]);
        } else {
          cand.put(k, chooseBestValue(k, values, cand.clone(), leftOvers));
        }
      } finally {
        // TODO: check constraints.
        result.add(cand);
        leftOvers.cover(tuplesCoveredBy(cand, this.strength));
      }
    }
  }

  private void vg(List<ValueTuple<String, Object>> result, LeftOvers leftOvers, String k, SortedMap<String, Object[]> factors) {
    for (ValueTuple toBeCovered : leftOvers.yetToCover()) {
      ValueTuple q = (ValueTuple) toBeCovered.clone();
      q.put(k, DontCare);
      List<ValueTuple<String, Object>> found = lookup(result, q);
      if (found.size() > 0) {
        Object value = toBeCovered.get(k);
        ValueTuple<String, Object> best = this.chooseBestTuple(found, leftOvers, k, value);
        best.put(k, value);
      }
      if (lookup(result, toBeCovered).size() == 0) {
        result.add(q.clone());
      }
    }
    List<String> allKeys = new ArrayList<String>(factors.size() + 1);
    allKeys.addAll(factors.keySet());
    allKeys.add(k);

    for (ValueTuple tuple : result) {
      if (tuple.size() < factors.size() + 1) {
        SortedMap<String, Object[]> missingFactors = new TreeMap<String, Object[]>();
        for (String key : factors.keySet()) {
          if (!tuple.containsKey(key)) {
            missingFactors.put(key, factors.get(key));
          }
        }
        addMissingValues(tuple, missingFactors, leftOvers);
      }
    }
  }

  private void addMissingValues(ValueTuple tuple, SortedMap<String, Object[]> missingFactors, LeftOvers leftOvers) {
    ////
    // TODO: Extension point
  }

  private ValueTuple<String, Object> chooseBestTuple(List<ValueTuple<String, Object>> found, LeftOvers leftOvers, String factor, Object levels) {
    ////
    // TODO: Extension point
    return null;
  }

  private Object chooseBestValue(String factor, Object[] levels, ValueTuple<String, Object> tuple, LeftOvers leftOvers) {
    ////
    // TODO: Extension point
    return null;
  }

  private String nthKey(int index, SortedMap<String, Object[]> domains) {
    Utils.checknotnull(domains);
    Utils.checkcond(index >= 0 && index < domains.size());
    Iterator<String> k = domains.keySet().iterator();
    for (int i = 0; i < domains.size(); i++) {
      String ret = k.next();
      if (i == index) {
        return ret;
      }
    }
    throw new RuntimeException();
  }


  static Set<ValueTuple<String, Object>> tuplesCoveredBy(ValueTuple<String, Object> tuple, int strength) {
    Set<ValueTuple<String, Object>> ret = new HashSet<ValueTuple<String, Object>>();
    Combinator<String> c = new Combinator<String>(new LinkedList<String>(tuple.keySet()), strength);
    for (List<String> keys : c) {
      ValueTuple<String, Object> cur = new ValueTuple<String, Object>();
      for (String k : keys) {
        cur.put(k, tuple.get(k));
      }
      ret.add(cur);
    }
    return ret;
  }

  private List<ValueTuple<String, Object>> lookup(List<ValueTuple<String, Object>> result, ValueTuple<String, Object> q) {
    List<ValueTuple<String, Object>> ret = new LinkedList<ValueTuple<String, Object>>();
    for (ValueTuple<String, Object> cur : result) {
      if (matches(cur, q))
        ret.add(cur);
    }
    return ret;
  }

  private boolean matches(ValueTuple<String, Object> tuple, ValueTuple<String, Object> q) {
    for (String k : q.keySet()) {
      Object v = q.get(k);
      if (v == DontCare && tuple.containsKey(k)) return false;
      if (!tuple.containsKey(k)) return false;
      if (!eq(v, tuple.get(k))) return false;
    }
    return true;
  }

  private boolean eq(Object v, Object o) {
    if (v == null) return o == null;
    return v.equals(o);
  }
}

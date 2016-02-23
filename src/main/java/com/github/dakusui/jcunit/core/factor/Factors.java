package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.combinatoradix.Combinator;
import com.github.dakusui.jcunit.core.BaseBuilder;
import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleImpl;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;

import java.util.*;

public class Factors implements Iterable<Factor> {
  private final List<Factor>        factors;
  private final Map<String, Factor> factorMap;

  public Factors(List<Factor> factors) {
    Checks.checknotnull(factors);
    this.factors = Collections.unmodifiableList(factors);
    Map<String, Factor> factorMap = new HashMap<String, Factor>();
    for (Factor f : factors) {
      Checks.checkcond(!factorMap.containsKey(f.name),
          "There are more than one factors whose names are '%s'.", f.name);
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
    Checks.checknotnull(factorName);
    return this.factorMap.get(factorName);
  }

  public Factor get(int index) {
    Checks.checkcond(index >= 0);
    Checks.checkcond(index < this.factors.size());
    return this.factors.get(index);
  }

  public boolean has(String factorName) {
    return this.factorMap.containsKey(factorName);
  }

  public String nextKey(String factorName) {
    Checks.checknotnull(factorName);
    Checks.checkcond(this.factorMap.containsKey(factorName));
    Factor f = get(factorName);
    int i = this.factors.indexOf(f);
    Checks.checkcond(i < this.factors.size() - 1,
        "'%s' is the last factor name.", factorName);
    Factor g = get(i + 1);
    return g.name;
  }

  public boolean isLastKey(String key) {
    return Utils.eq(key, this.factors.get(this.factors.size() - 1).name);
  }

  public Factors head(String to) {
    Checks.checknotnull(to);
    Checks.checkcond(has(to));
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
    Checks.checknotnull(from);
    Checks.checkcond(has(from));
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

  public List<Tuple> generateAllPossibleTuples(int strength) {
    return generateAllPossibleTuples(strength, new Utils.Predicate<Tuple>() {
      @Override
      public boolean apply(Tuple in) {
        return true;
      }
    });
  }

  public List<Tuple> generateAllPossibleTuples(
      int strength, Utils.Predicate<Tuple> predicate) {
    List<Tuple> ret = new LinkedList<Tuple>();
    Combinator<String> c = new Combinator<String>(this.getFactorNames(),
        strength);
    for (List<String> factorNames : c) {
      Factor[] chosenFactors = new Factor[factorNames.size()];
      int i = 0;
      for (String fName : factorNames) {
        chosenFactors[i++] = get(fName);
      }
      TupleUtils.CartesianTuples tuples = TupleUtils
          .enumerateCartesianProduct(new TupleImpl(), chosenFactors);
      ret.addAll(Utils.filter(tuples, predicate));
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
  public Tuple createTupleFrom(Tuple tuple, @SuppressWarnings("SameParameterValue") Object defaultValue) {
    Checks.checknotnull(tuple);
    for (String k : tuple.keySet()) {
      Checks.checkcond(this.factorMap.containsKey(k),
          "Undefined factor '%s' was found: defined keys (%s)", k,
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

  public List<Factor> asFactorList() {
    return this.factors; // It's unmodifiable already in the constructor.
  }

  public static class Builder implements BaseBuilder<Factors> {
    protected final List<Factor> factors;

    public Builder() {
      factors = new LinkedList<Factor>();
    }

    public Builder(List<Factor> factors) {
      Checks.checknotnull(factors);
      this.factors = new LinkedList<Factor>(factors);
    }

    public Builder add(Factor f) {
      Checks.checknotnull(f);
      this.factors.add(f);
      return this;
    }

    public Builder add(String factorName, Object first, Object... levels) {
      Factor.Builder b = new Factor.Builder(Checks.checknotnull(factorName));
      b.addLevel(first);
      for (Object each : levels) {
        b.addLevel(each);
      }
      return this.add(b.build());
    }

    public Factors build() {
      return new Factors(this.factors);
    }
  }
}

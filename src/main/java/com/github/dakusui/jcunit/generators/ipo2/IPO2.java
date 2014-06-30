package com.github.dakusui.jcunit.generators.ipo2;

import com.github.dakusui.enumerator.Combinator;
import com.github.dakusui.enumerator.tuple.AttrValue;
import com.github.dakusui.enumerator.tuple.CartesianEnumerator;
import com.github.dakusui.jcunit.constraints.ConstraintManager;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.ValueTuple;
import com.github.dakusui.jcunit.generators.ipo.GiveUp;
import com.github.dakusui.jcunit.generators.ipo2.optimizers.IPO2Optimizer;

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

  private final ConstraintManager                constraintManager;
  private final LinkedHashMap
                    <String, Object[]>           domains;
  private final int                              strength;
  private final IPO2Optimizer                    optimizer;
  private       List<ValueTuple<String, Object>> result;

  public IPO2(LinkedHashMap<String, Object[]> domains, int strength,
      ConstraintManager constraintManager, IPO2Optimizer optimizer) {
    Utils.checknotnull(domains);
    Utils.checkcond(domains.size() >= strength);
    Utils.checkcond(strength >= 2);
    Utils.checknotnull(constraintManager);
    Utils.checknotnull(optimizer);
    this.domains = domains;
    this.strength = strength;
    this.result = null;
    this.constraintManager = constraintManager;
    this.optimizer = optimizer;
  }

  static Set<ValueTuple<String, Object>> tuplesCoveredBy(
      ValueTuple<String, Object> tuple, int strength) {
    Set<ValueTuple<String, Object>> ret = new HashSet<ValueTuple<String, Object>>();
    Combinator<String> c = new Combinator<String>(
        new LinkedList<String>(tuple.keySet()), strength);
    for (List<String> keys : c) {
      ValueTuple<String, Object> cur = new ValueTuple<String, Object>();
      for (String k : keys) {
        cur.put(k, tuple.get(k));
      }
      ret.add(cur);
    }
    return ret;
  }

  private static List<ValueTuple<String, Object>> lookup(
      List<ValueTuple<String, Object>> tuples, ValueTuple<String, Object> q) {
    List<ValueTuple<String, Object>> ret = new LinkedList<ValueTuple<String, Object>>();
    for (ValueTuple<String, Object> cur : tuples) {
      if (matches(cur, q)) {
        ret.add(cur);
      }
    }
    return ret;
  }

  private static boolean matches(ValueTuple<String, Object> tuple,
      ValueTuple<String, Object> q) {
    for (String k : q.keySet()) {
      Object v = q.get(k);
      if (v == DontCare && tuple.containsKey(k)) {
        return false;
      }
      if (!tuple.containsKey(k)) {
        return false;
      }
      if (!IPO2Utils.eq(v, tuple.get(k))) {
        return false;
      }
    }
    return true;
  }

  static private List<ValueTuple<String, Object>> cloneTuples(
      List<ValueTuple<String, Object>> found) {
    List<ValueTuple<String, Object>> ret = new ArrayList<ValueTuple<String, Object>>(
        found.size());
    for (ValueTuple<String, Object> cur : found) {
      ret.add(cur);
    }
    return ret;
  }

  public void ipo() {
    if (this.strength < this.domains.size()) {
      this.result = initialTestCases(
          IPO2Utils
              .headMap(domains, IPO2Utils.nthKey(this.strength, this.domains))
      );
    } else if (domains.size() == this.strength) {
      this.result = initialTestCases(this.domains);
      return;
    }

    for (String factorName :
        IPO2Utils
            .tailMap(this.domains, IPO2Utils.nthKey(strength, this.domains))
            .keySet()) {
      ////
      // Initialize a set that holds all the tuples to be covered in this
      // iteration.
      LeftTuples leftTuples = new LeftTuples(domains, factorName,
          this.strength);

      ////
      // Expand test case set horizontally and get the list of test cases
      // that are proven to be invalid.
      hg(result, leftTuples, factorName, domains.get(factorName));

      if (leftTuples.isEmpty()) {
        continue;
      }
      vg(result, leftTuples, factorName,
          IPO2Utils.headMap(domains, factorName));
    }
  }

  public List<ValueTuple<String, Object>> getResult() {
    Utils.checkcond(this.result != null, "Execute ipo() method first");
    return Collections.unmodifiableList(this.result);
  }

  private List<ValueTuple<String, Object>> initialTestCases(
      LinkedHashMap<String, Object[]> domains) {
    List<AttrValue<String, Object>> attrValues = new LinkedList<AttrValue<String, Object>>();
    for (String k : domains.keySet()) {
      for (Object v : domains.get(k)) {
        attrValues.add(new AttrValue<String, Object>(k, v));
      }
    }

    CartesianEnumerator<String, Object> ce = new CartesianEnumerator<String, Object>(
        attrValues);
    List<ValueTuple<String, Object>> ret = new ArrayList<ValueTuple<String, Object>>(
        (int) ce.size());
    for (List<AttrValue<String, Object>> t : ce) {
      ValueTuple<String, Object> tuple = IPO2Utils.list2tuple(t);
      if (constraintManager.check(tuple)) {
        ret.add(tuple);
      }
    }
    return ret;
  }

  /*
   * Returns a list of test cases in {@code result} which are proven to be not
   * possible under given constraints.
   */
  private void hg(
      List<ValueTuple<String, Object>> result, LeftTuples leftTuples,
      String factorName, Object[] factorLevels) {
    List<ValueTuple<String, Object>> invalidTests = new LinkedList<ValueTuple<String, Object>>();
    for (int i = 0; i < factorLevels.length; i++) {
      ValueTuple<String, Object> cand = result.get(i);
      Object chosenLevel;
      List<Object> levelList = Arrays.asList(factorLevels);
      for (int j = 0; j < factorLevels.length; j++) {
        if (i < result.size()) {
          chosenLevel = factorLevels[(i + j) % factorLevels.length];
        } else {
          chosenLevel = chooseBestValue(factorName, levelList.toArray(),
              cand.clone(), leftTuples);
        }
        if (constraintManager.check(cand)) {
          cand.put(factorName, chosenLevel);
          result.add(cand);
          leftTuples.removeAll(tuplesCoveredBy(cand, this.strength));
        } else {
          levelList.remove(chosenLevel);
          if (levelList.isEmpty()) {
            cand.remove(factorName);
            invalidTests.add(cand);
          }
        }
      }
    }
    ////
    // Remove tuples covered by invalid tests unless they are covered by other
    // tests.
    // 1. Remove invalid tests from 'result'.
    for (ValueTuple<String, Object> cur : invalidTests) {
      result.remove(cur);
    }
    // 2. Calculate all the tuples covered by the invalid tests.
    Set<ValueTuple<String, Object>> invalidTuples = new HashSet<ValueTuple<String, Object>>();
    for (ValueTuple<String, Object> c : invalidTests) {
      invalidTuples.addAll(tuplesCoveredBy(c, this.strength));
    }
    // 3. Check if each tuple is covered by remaining tests in 'result' and
    //    if not, it will be added to 'leftTuples' again.
    for (ValueTuple<String, Object> c : invalidTuples) {
      if (lookup(result, c).isEmpty()) {
        leftTuples.add(c);
      }
    }
  }

  private Set<ValueTuple<String, Object>> vg(
      List<ValueTuple<String, Object>> result,
      LeftTuples leftTuples,
      String factorName, LinkedHashMap<String, Object[]> factors) {
    for (ValueTuple toBeCovered : leftTuples.yetToCover()) {
      ValueTuple q = toBeCovered.clone();
      q.put(factorName, DontCare);
      List<ValueTuple<String, Object>> found = lookup(result, q);
      ValueTuple<String, Object> best = null;
      if (lookup(result, toBeCovered).size() > 0) {
        Object value = toBeCovered.get(factorName);
        List<ValueTuple<String, Object>> foundTuples = cloneTuples(found);
        while (!foundTuples.isEmpty()) {
          ValueTuple<String, Object> chosen = this
              .chooseBestTuple(foundTuples, leftTuples, factorName, value);
          Utils.checknotnull(best);
          Utils.checkcond(found.contains(chosen),
              "User code must return a value from found tuples.");
          chosen.put(factorName, value);
          if (this.constraintManager.check(chosen)) {
            best = chosen;
            break;
          } else {
            foundTuples.remove(chosen);
          }
        }
      } else {
        ValueTuple<String, Object> t = createTupleFrom(q.clone(),
            factors.keySet());
        if (this.constraintManager.check(t)) {
          best = t;
        }
      }
      if (best != null) {
        ////
        // Unlike original IPO algorithm, in case implied constraint violation
        // is detected, tuples that do not involve factor 'factorName' can be in
        // 'LeftTuples'.
        // So we need to list up all possible tuples and remove them from it.
        leftTuples.removeAll(tuplesCoveredBy(best, this.strength));
        result.add(best);
      }
    }
    List<String> allKeys = new ArrayList<String>(factors.size() + 1);
    allKeys.addAll(factors.keySet());
    allKeys.add(factorName);

    Set<ValueTuple<String, Object>> ret = new HashSet<ValueTuple<String, Object>>();
    for (ValueTuple tuple : result) {
      if (tuple.size() < factors.size() + 1) {
        LinkedHashMap<String, Object[]> missingFactors = new LinkedHashMap<String, Object[]>();
        for (String key : factors.keySet()) {
          if (!tuple.containsKey(key)) {
            missingFactors.put(key, factors.get(key));
          }
        }
        try {
          fillInMissingFactors(tuple, missingFactors, leftTuples,
              this.constraintManager);
        } catch (GiveUp e) {
          ret.add(e.getTuple());
        }
      }
    }
    return ret;
  }

  /**
   * An extension point.
   */
  protected ValueTuple<String, Object> fillInMissingFactors(ValueTuple tuple,
      LinkedHashMap<String, Object[]> missingFactors, LeftTuples leftTuples,
      ConstraintManager<String, Object> constraintManager) {
    Utils.checknotnull(tuple);
    Utils.checknotnull(missingFactors);
    Utils.checknotnull(leftTuples);
    Utils.checknotnull(constraintManager);
    return this.optimizer
        .fillInMissingFactors(tuple, missingFactors, leftTuples,
            constraintManager);
  }

  /**
   * An extension point.
   * Called by 'vg' process.
   * Chooses the best tuple to assign the factor and its level from the given tests.
   *
   * @param found A list of cloned tuples. (candidates)
   */
  protected ValueTuple<String, Object> chooseBestTuple(
      List<ValueTuple<String, Object>> found, LeftTuples leftTuples,
      String factorName, Object level) {
    Utils.checknotnull(found);
    Utils.checkcond(found.size() > 0);
    Utils.checknotnull(leftTuples);
    Utils.checknotnull(factorName);
    return this.optimizer.chooseBestTuple(found, leftTuples, factorName, level);
  }

  /**
   * An extension point.
   * Called by 'hg' process.
   */
  protected Object chooseBestValue(String factorName, Object[] factorLevels,
      ValueTuple<String, Object> tuple, LeftTuples leftTuples) {
    Utils.checknotnull(factorLevels);
    Utils.checknotnull(factorLevels);
    Utils.checknotnull(tuple);
    Utils.checknotnull(leftTuples);
    return this.optimizer
        .chooseBestValue(factorName, factorLevels, tuple, leftTuples);
  }

  /**
   * Creates a new tuple from a given tuple.
   *
   * @param base        A tuple from which new tuple is based
   * @param factorNames Factors set to 'DontCare'
   */
  private ValueTuple<String, Object> createTupleFrom(
      ValueTuple<String, Object> base, Set<String> factorNames) {
    ValueTuple<String, Object> ret = base.clone();
    for (String f : factorNames) {
      if (!ret.containsKey(f)) {
        ret.put(f, DontCare);
      }
    }
    return ret;
  }
}

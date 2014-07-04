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

public class IPO2 {
  public static final Object DontCare = new Object() {
    @Override
    public String toString() {
      return "D/C";
    }
  };

  private final ConstraintManager<String, Object> constraintManager;
  private final LinkedHashMap
                    <String, Object[]>            factors;
  private final int                               strength;
  private final IPO2Optimizer                     optimizer;
  private       List<ValueTuple<String, Object>>  result;

  public IPO2(LinkedHashMap<String, Object[]> factors, int strength,
      ConstraintManager<String, Object> constraintManager,
      IPO2Optimizer optimizer) {
    Utils.checknotnull(factors);
    Utils.checkcond(factors.size() >= strength, String.format("The strength must be greater than 1 and less than %d.", factors.size()));
    Utils.checkcond(strength >= 2, String.format("The strength must be greater than 1 and less than %d.", factors.size()));
    Utils.checknotnull(constraintManager);
    Utils.checknotnull(optimizer);
    for (String k : factors.keySet()) {
      Utils.checknotnull(k);
      Utils.checknotnull(factors.get(k));
      Utils.checkcond(factors.get(k).length != 0,
          "Each domain must have at least one level.");
    }
    this.factors = factors;
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
      if (!tuple.containsKey(k) || !IPO2Utils.eq(q.get(k), tuple.get(k))) {
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
    if (this.strength < this.factors.size()) {
      this.result = initialTestCases(
          IPO2Utils
              .headMap(factors, IPO2Utils.nthKey(this.strength, this.factors))
      );
    } else if (factors.size() == this.strength) {
      this.result = initialTestCases(this.factors);
      return;
    }

    LinkedHashSet<ValueTuple<String, Object>> leftOver = new LinkedHashSet<ValueTuple<String, Object>>();
    for (String factorName :
        IPO2Utils
            .tailMap(this.factors, IPO2Utils.nthKey(strength, this.factors))
            .keySet()) {
      ////
      // Initialize a set that holds all the tuples to be covered in this
      // iteration.
      LeftTuples leftTuples = new LeftTuples(factors, factorName,
          this.strength);
      leftTuples.addAll(leftOver);

      ////
      // Expand test case set horizontally and get the list of test cases
      // that are proven to be invalid.
      hg(result, leftTuples, factorName, factors.get(factorName));

      if (leftTuples.isEmpty()) {
        continue;
      }
      leftOver = vg(result, leftTuples, factorName,
          IPO2Utils.headMap(factors, factorName));
    }
    ////
    // As a result of replacing don't care values, multiple test cases can be identical.
    // By registering all the members to a new temporary set and adding them back to
    // the original one, I'm removing those duplicates.
    LinkedHashSet<ValueTuple<String, Object>> tmp = new LinkedHashSet<ValueTuple<String, Object>>(
        result);
    result.clear();
    result.addAll(tmp);
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
    for (int i = 0; i < result.size(); i++) {
      ValueTuple<String, Object> cur = result.get(i);
      Object chosenLevel = null;
      List<Object> levelList = Arrays.asList(factorLevels);
      for (int j = 0; j < factorLevels.length; j++) {
        if (i < result.size()) {
          chosenLevel = factorLevels[(i + j) % factorLevels.length];
        } else {
          chosenLevel = chooseBestValue(
              factorName,
              levelList,
              cur,
              leftTuples);
        }
      }
      cur.put(factorName, chosenLevel);
      if (constraintManager.check(cur)) {
        leftTuples.removeAll(tuplesCoveredBy(cur, this.strength));
      } else {
        levelList.remove(chosenLevel);
        if (levelList.isEmpty()) {
          cur.remove(factorName);
          invalidTests.add(cur);
          break;
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

  private LinkedHashSet<ValueTuple<String, Object>> vg(
      List<ValueTuple<String, Object>> result,
      LeftTuples leftTuples,
      String factorName, LinkedHashMap<String, Object[]> factors) {
    for (ValueTuple<String, Object> toBeCovered : leftTuples.yetToCover()) {
      ValueTuple<String, Object> q = toBeCovered.clone();
      q.put(factorName, DontCare);
      List<ValueTuple<String, Object>> found = lookup(result, q);
      ValueTuple<String, Object> best = null;
      if (lookup(result, toBeCovered).size() > 0) {
        Object value = toBeCovered.get(factorName);
        List<ValueTuple<String, Object>> foundTuples = cloneTuples(found);
        while (!foundTuples.isEmpty()) {
          ValueTuple<String, Object> chosen = this
              .chooseBestTuple(foundTuples, leftTuples, factorName, value);
          chosen.put(factorName, value);
          if (this.constraintManager.check(chosen)) {
            best = chosen;
            break;
          } else {
            foundTuples.remove(chosen);
          }
        }
      } else {
        ValueTuple<String, Object> t = createTupleFrom(q,
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
    LinkedHashSet<ValueTuple<String, Object>> ret = new LinkedHashSet<ValueTuple<String, Object>>();
    for (ValueTuple<String, Object> testCase : result) {
      try {
        ValueTuple<String, Object> processedTestCase = fillInMissingFactors(
            testCase,
            leftTuples,
            this.constraintManager);
        testCase.putAll(processedTestCase);
        leftTuples.removeAll(tuplesCoveredBy(testCase, strength));
      } catch (GiveUp e) {
        ret.add(e.getTuple());
      }
    }
    return ret;
  }

  /**
   * An extension point.
   */
  protected ValueTuple<String, Object> fillInMissingFactors(ValueTuple<String, Object> tuple,
      LeftTuples leftTuples,
      ConstraintManager<String, Object> constraintManager) {
    Utils.checknotnull(tuple);
    Utils.checknotnull(leftTuples);
    Utils.checknotnull(constraintManager);
    ValueTuple<String, Object> ret = this.optimizer
        .fillInMissingFactors(tuple.clone(), leftTuples,
            constraintManager, this.factors);
    Utils.checknotnull(ret);
    Utils.checkcond(ret.keySet().equals(tuple.keySet()));
    Utils.checkcond(!ret.containsValue(DontCare));
    Utils.checkcond(constraintManager.check(ret));
    return ret;
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
    ValueTuple<String, Object> ret = this.optimizer
        .chooseBestTuple(new LinkedList<ValueTuple<String, Object>>(found),
            leftTuples, factorName, level);
    Utils.checknotnull(ret);
    Utils.checkcond(found.contains(ret),
        "User code must return a value from found tuples.");
    return ret;
  }

  /**
   * An extension point.
   * Called by 'hg' process.
   */
  protected Object chooseBestValue(String factorName, List<Object> factorLevels,
      ValueTuple<String, Object> tuple, LeftTuples leftTuples) {
    Utils.checknotnull(factorName);
    Utils.checknotnull(factorLevels);
    Utils.checkcond(factorLevels.size() > 0);
    Utils.checknotnull(tuple);
    Utils.checknotnull(leftTuples);

    Object ret = this.optimizer
        .chooseBestValue(factorName, factorLevels.toArray() /* By specification of 'toArray', even if the content is modified, it's safe */,
            tuple.clone() /* In order to prevent plugins from breaking this tuple, clone it. */,
            leftTuples);
    Utils.checkcond(factorLevels.contains(ret));
    return ret;
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

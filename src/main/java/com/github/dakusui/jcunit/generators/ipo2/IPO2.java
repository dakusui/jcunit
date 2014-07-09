package com.github.dakusui.jcunit.generators.ipo2;

import com.github.dakusui.enumerator.tuple.AttrValue;
import com.github.dakusui.enumerator.tuple.CartesianEnumerator;
import com.github.dakusui.jcunit.compat.generators.ipo.GiveUp;
import com.github.dakusui.jcunit.constraints.ConstraintManager;
import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.generators.ipo2.optimizers.IPO2Optimizer;

import java.util.*;

public class IPO2 {
  public static final Object DontCare = new Object() {
    @Override
    public String toString() {
      return "D/C";
    }
  };

  private final ConstraintManager constraintManager;
  private final Factors           factors;
  private final int               strength;
  private final IPO2Optimizer     optimizer;
  private       List<Tuple>       result;

  public IPO2(Factors factors, int strength,
      ConstraintManager constraintManager,
      IPO2Optimizer optimizer) {
    Utils.checknotnull(factors);
    Utils.checkcond(factors.size() >= strength, String.format(
        "The strength must be greater than 1 and less than %d.",
        factors.size()));
    Utils.checkcond(strength >= 2, String
        .format("The strength must be greater than 1 and less than %d.",
            factors.size()));
    Utils.checknotnull(constraintManager);
    Utils.checknotnull(optimizer);
    this.factors = factors;
    this.strength = strength;
    this.result = null;
    this.constraintManager = constraintManager;
    this.optimizer = optimizer;
  }

  private static List<Tuple> lookup(
      List<Tuple> tuples, Tuple q) {
    List<Tuple> ret = new LinkedList<Tuple>();
    for (Tuple cur : tuples) {
      if (matches(cur, q)) {
        ret.add(cur.clone());
      }
    }
    return ret;
  }

  private static boolean matches(Tuple tuple,
      Tuple q) {
    for (String k : q.keySet()) {
      if (!tuple.containsKey(k) || !IPO2Utils.eq(q.get(k), tuple.get(k))) {
        return false;
      }
    }
    return true;
  }

  private List<Tuple> filterInvalidTuples(
      List<Tuple> tuples) {
    List<Tuple> ret = new ArrayList<Tuple>(tuples.size());
    for (Tuple cur : tuples) {
      if (checkConstraints(cur)) {
        ret.add(cur);
      }
    }
    return ret;
  }

  public void ipo() {
    if (this.strength < this.factors.size()) {
      this.result = initialTestCases(
          factors.head(factors.get(this.strength).name)
      );
    } else if (factors.size() == this.strength) {
      this.result = initialTestCases(this.factors);
      return;
    }

    Set<Tuple> leftOver = new LinkedHashSet<Tuple>();
    for (String factorName :
        this.factors.tail(this.factors.get(this.strength).name)
            .getFactorNames()) {
      ////
      // Initialize a set that holds all the tuples to be covered in this
      // iteration.
      Tuples leftTuples = new Tuples(factors.head(factorName),
          factors.get(factorName),
          this.strength);
      leftTuples.addAll(leftOver);

      ////
      // Expand test case set horizontally and get the list of test cases
      // that are proven to be invalid.
      hg(result, leftTuples, factors.get(factorName));

      if (leftTuples.isEmpty()) {
        continue;
      }
      if (factors.isLastKey(factorName)) {
        leftOver = vg(result, leftTuples, factors);
      } else {
        leftOver = vg(result, leftTuples,
            factors.head(factors.nextKey(factorName)));
      }
    }
    ////
    // As a result of replacing don't care values, multiple test cases can be identical.
    // By registering all the members to a new temporary set and adding them back to
    // the original one, I'm removing those duplicates.
    LinkedHashSet<Tuple> tmp = new LinkedHashSet<Tuple>(result);
    result.clear();
    result.addAll(tmp);
  }

  public List<Tuple> getResult() {
    Utils.checkcond(this.result != null, "Execute ipo() method first");
    return Collections.unmodifiableList(this.result);
  }

  private List<Tuple> initialTestCases(
      Factors factors) {
    List<AttrValue<String, Object>> attrValues = new LinkedList<AttrValue<String, Object>>();
    for (String k : factors.getFactorNames()) {
      for (Object v : factors.get(k)) {
        attrValues.add(new AttrValue<String, Object>(k, v));
      }
    }

    CartesianEnumerator<String, Object> ce = new CartesianEnumerator<String, Object>(
        attrValues);
    List<Tuple> ret = new ArrayList<Tuple>(
        (int) ce.size());
    for (List<AttrValue<String, Object>> t : ce) {
      Tuple tuple = IPO2Utils.list2tuple(t);
      if (checkConstraints(tuple)) {
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
      List<Tuple> result, Tuples leftTuples, Factor factor) {
    List<Tuple> invalidTests = new LinkedList<Tuple>();
    String factorName = factor.name;
    Object[] factorLevels = factor.levels.toArray();
    for (int i = 0; i < result.size(); i++) {
      Tuple cur = result.get(i);
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
      if (checkConstraints(cur)) {
        leftTuples.removeAll(IPO2Utils.subtuplesOf(cur, this.strength));
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
    for (Tuple cur : invalidTests) {
      result.remove(cur);
    }
    // 2. Calculate all the tuples covered by the invalid tests.
    Set<Tuple> invalidTuples = new HashSet<Tuple>();
    for (Tuple c : invalidTests) {
      invalidTuples.addAll(IPO2Utils.subtuplesOf(c, this.strength));
    }
    // 3. Check if each tuple is covered by remaining tests in 'result' and
    //    if not, it will be added to 'leftTuples' again.
    for (Tuple c : invalidTuples) {
      if (lookup(result, c).isEmpty()) {
        leftTuples.add(c);
      }
    }
  }

  private Set<Tuple> vg(
      List<Tuple> result,
      Tuples leftTuples,
      Factors factors) {
    Set<Tuple> ret = new LinkedHashSet<Tuple>();
    List<Tuple> work = leftTuples.leftTuples();
    for (Tuple cur : work) {
      if (leftTuples.isEmpty()) {
        break;
      }
      if (!leftTuples.contains(cur)) {
        continue;
      }
      Tuple best;
      int numCovered;
      Tuple t = factors.createTupleFrom(cur, DontCare);
      if (checkConstraints(t)) {
        best = t;
        numCovered = leftTuples.coveredBy(t).size();
      } else {
        ///
        // This tuple can't be covered at all. Because it is explicitly violate
        // given constraints.
        ret.add(cur);
        continue;
      }
      for (String factorName : cur.keySet()) {
        Tuple q = cur.clone();
        q.put(factorName, DontCare);
        List<Tuple> found = filterInvalidTuples(
            lookup(result, q));

        if (found.size() > 0) {
          Object levelToBeAssigned = cur.get(factorName);
          Tuple f = this
              .chooseBestTuple(found, leftTuples, factorName,
                  levelToBeAssigned);
          f.put(factorName, levelToBeAssigned);
          int num = leftTuples.coveredBy(f).size();
          if (num > numCovered) {
            numCovered = num;
            best = f;
          }
        }
        // In case no matching tuple is found, fall back to the best known
        // tuple.
      }
      leftTuples.removeAll(IPO2Utils.subtuplesOf(best, this.strength));
      result.add(best);
    }
    for (Tuple testCase : result) {
      try {
        Tuple processedTestCase = fillInMissingFactors(
            testCase,
            leftTuples);
        testCase.putAll(processedTestCase);
        leftTuples.removeAll(IPO2Utils.subtuplesOf(testCase, strength));
      } catch (GiveUp e) {
        ret.add(e.getTuple());
      }
    }
    return ret;
  }

  /**
   * An extension point.
   */
  protected Tuple fillInMissingFactors(
      Tuple tuple,
      Tuples leftTuples) {
    Utils.checknotnull(tuple);
    Utils.checknotnull(leftTuples);
    Utils.checknotnull(constraintManager);
    Tuple ret = this.optimizer
        .fillInMissingFactors(tuple.clone(), leftTuples,
            constraintManager, this.factors);
    Utils.checknotnull(ret);
    Utils.checkcond(ret.keySet().equals(tuple.keySet()));
    Utils.checkcond(!ret.containsValue(DontCare));
    Utils.checkcond(checkConstraints(ret));
    return ret;
  }

  private boolean checkConstraints(Tuple cur) {
    Utils.checknotnull(cur);
    Tuple tuple = cur.clone();
    for (String factorName : cur.keySet()) {
      if (tuple.get(factorName) == DontCare) {
        tuple.remove(factorName);
      }
    }
    return constraintManager.check(cur);
  }

  /**
   * An extension point.
   * Called by 'vg' process.
   * Chooses the best tuple to assign the factor and its level from the given tests.
   * This method itself doesn't assign {@code level} to {@code factorName}.
   *
   * @param found A list of cloned tuples. (candidates)
   */
  protected Tuple chooseBestTuple(
      List<Tuple> found, Tuples leftTuples,
      String factorName, Object level) {
    Utils.checknotnull(found);
    Utils.checkcond(found.size() > 0);
    Utils.checknotnull(leftTuples);
    Utils.checknotnull(factorName);
    Tuple ret = this.optimizer
        .chooseBestTuple(Collections.unmodifiableList(found),
            leftTuples.unmodifiableVersion(), factorName, level);
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
      Tuple tuple, Tuples leftTuples) {
    Utils.checknotnull(factorName);
    Utils.checknotnull(factorLevels);
    Utils.checkcond(factorLevels.size() > 0);
    Utils.checknotnull(tuple);
    Utils.checknotnull(leftTuples);

    Object ret = this.optimizer
        .chooseBestValue(factorName, factorLevels.toArray() /* By specification of 'toArray', even if the content is modified, it's safe */,
            tuple.clone() /* In order to prevent plugins from breaking this tuple, clone it. */,
            leftTuples.unmodifiableVersion());
    Utils.checkcond(factorLevels.contains(ret));
    return ret;
  }
}

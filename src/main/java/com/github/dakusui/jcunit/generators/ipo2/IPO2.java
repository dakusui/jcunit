package com.github.dakusui.jcunit.generators.ipo2;

import com.github.dakusui.enumerator.tuple.AttrValue;
import com.github.dakusui.enumerator.tuple.CartesianEnumerator;
import com.github.dakusui.jcunit.compat.generators.ipo.GiveUp;
import com.github.dakusui.jcunit.constraints.ConstraintManager;
import com.github.dakusui.jcunit.core.Tuple;
import com.github.dakusui.jcunit.core.Tuples;
import com.github.dakusui.jcunit.core.Utils;
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
  private       List<Tuple>       remainders;

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
    this.remainders = null;
    this.constraintManager = constraintManager;
    this.optimizer = optimizer;
  }

  private static List<Tuple> lookup(
      List<Tuple> tuples, Tuple q) {
    List<Tuple> ret = new LinkedList<Tuple>();
    for (Tuple cur : tuples) {
      if (matches(cur, q)) {
        ret.add(cur.cloneTuple());
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
      this.remainders = new LinkedList<Tuple>();
      this.result = initialTestCases(
          factors.head(factors.get(this.strength).name)
      );
    } else if (factors.size() == this.strength) {
      this.remainders = new LinkedList<Tuple>();
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

      System.out.println("HG:result  =" + result);
      System.out.println("HG:leftover=" + leftOver);

      ////
      // Expand test case set horizontally and get the list of test cases
      // that are proven to be invalid.
      leftOver = hg(result, leftTuples, factors.get(factorName));
      leftTuples.removeAll(leftOver);
      if (leftTuples.isEmpty()) {
        continue;
      }
      if (factors.isLastKey(factorName)) {
        leftOver = vg(result, leftTuples, factors);
      } else {
        leftOver = vg(result, leftTuples,
            factors.head(factors.nextKey(factorName)));
      }
      System.out.println("VG:result  =" + result);
      System.out.println("VG:leftover=" + leftOver);
    }
    ////
    // As a result of replacing don't care values, multiple test cases can be identical.
    // By registering all the members to a new temporary set and adding them back to
    // the original one, I'm removing those duplicates.
    LinkedHashSet<Tuple> tmp = new LinkedHashSet<Tuple>(result);
    this.result.clear();
    this.result.addAll(tmp);
    this.remainders.addAll(leftOver);
  }

  public List<Tuple> getResult() {
    Utils.checkcond(this.result != null, "Execute ipo() method first");
    return Collections.unmodifiableList(this.result);
  }

  public List<Tuple> getRemainders() {
    Utils.checkcond(this.result != null, "Execute ipo() method first");
    return Collections.unmodifiableList(this.remainders);
  }

  private List<Tuple> initialTestCases(
      Factors factors) {
    List<AttrValue<String, Object>> attrValues = new ArrayList<AttrValue<String, Object>>(
        512);
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
  private Set<Tuple> hg(
      List<Tuple> result, Tuples leftTuples, Factor factor) {
    Set<Tuple> leftOver = new HashSet<Tuple>();
    List<Tuple> invalidTests = new LinkedList<Tuple>();
    String factorName = factor.name;
    // Factor levels to cover in this method.
    for (int i = 0; i < result.size(); i++) {
      Tuple cur = result.get(i);
      Object chosenLevel;
      // Since Arrays.asList returns an unmodifiable list,
      // create another list to hold
      List<Object> possibleLevels = new LinkedList<Object>(factor.levels);
      boolean validLevelFound = false;
      while (!possibleLevels.isEmpty()) {
        chosenLevel = chooseBestValue(
            factorName,
            possibleLevels,
            cur,
            leftTuples);
        cur.put(factorName, chosenLevel);
        if (checkConstraints(cur)) {
          leftTuples.removeAll(IPO2Utils.subtuplesOf(cur, this.strength));
          validLevelFound = true;
          break;
        } else {
          cur.remove(factorName);
        }
        possibleLevels.remove(chosenLevel);
      }
      if (!validLevelFound) {
        // A testCase cur can't be covered.
        Tuple tupleGivenUp = cur.cloneTuple();
        cur.clear();
        handleGivenUpTuple(tupleGivenUp, result, leftOver);
        invalidTests.add(cur);
      }
    }
    ////
    // Remove empty tests from the result.
    for (Tuple cur : invalidTests) {
      result.remove(cur);
    }
    return leftOver;
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
        // This tuple can't be covered at all. Because it is explicitly violating
        // given constraints.
        ret.add(cur);
        continue;
      }
      for (String factorName : cur.keySet()) {
        Tuple q = cur.cloneTuple();
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
      Set<Tuple> subtuplesOfBest = IPO2Utils.subtuplesOf(best, this.strength);
      leftTuples.removeAll(subtuplesOfBest);
      ret.removeAll(subtuplesOfBest);
      result.add(best);
    }
    Set<Tuple> remove = new HashSet<Tuple>();
    for (Tuple testCase : result) {
      try {
        fillInMissingFactors(testCase, leftTuples);
        Set<Tuple> subtuples = IPO2Utils.subtuplesOf(testCase, strength);
        leftTuples.removeAll(subtuples);
        ret.removeAll(subtuples);
      } catch (GiveUp e) {
        Tuple tupleGivenUp = removeDontCareEntries(e.getTuple().cloneTuple());
        testCase.clear();
        handleGivenUpTuple(tupleGivenUp, result, ret);
        remove.add(testCase);
      }
    }
    result.removeAll(remove);
    return ret;
  }

  private void handleGivenUpTuple(Tuple tupleGivenUp, List<Tuple> result,
      Set<Tuple> leftOver) {
    for (Tuple invalidatedSubTuple : IPO2Utils
        .subtuplesOf(tupleGivenUp, strength)) {
      if (lookup(result, invalidatedSubTuple).size() == 0) {
        leftOver.add(invalidatedSubTuple);
      }
    }
  }

  /**
   * Calls an extension point in optimizer 'fillInMissingFactors'.
   * Update content of {@code tuple} using optimizer.
   * Throws a {@code GiveUp} when this method can't find a valid tuple.
   */
  protected void fillInMissingFactors(
      Tuple tuple,
      Tuples leftTuples) {
    Utils.checknotnull(tuple);
    Utils.checknotnull(leftTuples);
    Utils.checknotnull(constraintManager);
    Tuple work = this.optimizer
        .fillInMissingFactors(tuple.cloneTuple(), leftTuples,
            constraintManager, this.factors);
    Utils.checknotnull(work);
    Utils.checkcond(work.keySet().equals(tuple.keySet()));
    Utils.checkcond(!work.containsValue(DontCare));
    if (!checkConstraints(work)) {
      throw new GiveUp(removeDontCareEntries(work));
    }
    tuple.putAll(work);
  }

  private boolean checkConstraints(Tuple cur) {
    Utils.checknotnull(cur);
    return constraintManager.check(removeDontCareEntries(cur));
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
        .chooseBestTuple(found,
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
            tuple, leftTuples.unmodifiableVersion());
    Utils.checkcond(factorLevels.contains(ret));
    return ret;
  }

  private Tuple removeDontCareEntries(Tuple cur) {
    Tuple tuple = cur.cloneTuple();
    for (String factorName : cur.keySet()) {
      if (tuple.get(factorName) == DontCare) {
        tuple.remove(factorName);
      }
    }
    return tuple;
  }
}

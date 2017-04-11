package com.github.dakusui.jcunit.plugins.caengines.ipo2;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.core.tuples.Tuples;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.exceptions.GiveUp;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.caengines.ipo2.optimizers.Ipo2Optimizer;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;

import java.util.*;

public class Ipo2 extends Ipo {

  private final Ipo2Optimizer optimizer;

  public Ipo2(Factors factors, int strength,
      ConstraintChecker constraintChecker,
      Ipo2Optimizer optimizer) {
    super(strength, constraintChecker, factors);
    Checks.checknotnull(factors);
    Checks.checkcond(factors.size() >= 2, "There must be 2 or more factors.");
    Checks.checkcond(factors.size() >= strength, "The strength must be greater than 1 and less than %d.", factors.size());
    Checks.checkcond(strength >= 2, "The strength must be greater than 1 and less than %d.", factors.size());
    Checks.checknotnull(constraintChecker);
    Checks.checknotnull(optimizer);
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
      if (!tuple.containsKey(k) || !Utils.eq(q.get(k), tuple.get(k))) {
        return false;
      }
    }
    return true;
  }

  private List<Tuple> filterInvalidTuples(
      List<Tuple> tuples) {
    List<Tuple> ret = new ArrayList<Tuple>(tuples.size());
    for (Tuple cur : tuples) {
      if (makeSureTupleDoesnotViolateAnyConstraintExplicitly(cur)) {
        ret.add(cur);
      }
    }
    return ret;
  }

  @Override
  public Result ipo() {
    if (factors.size() == this.strength) {
      return new Result(
          generateAllPossibleTuples(this.factors.asFactorList(), Utils.<Tuple>alwaysTrue()),
          Collections.<Tuple>emptyList());
    }
    List<Tuple> generatedTuples = generateAllPossibleTuples(
        factors.head(factors.get(this.strength).name).asFactorList(),
        Utils.<Tuple>alwaysTrue());
    List<Tuple> remainderTuples = new LinkedList<Tuple>();

    Set<Tuple> leftOver = new LinkedHashSet<Tuple>();
    for (String factorName : this.factors.tail(this.factors.get(this.strength).name).getFactorNames()) {
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
      leftOver = hg(generatedTuples, leftTuples, factors.get(factorName));
      if (leftTuples.isEmpty()) {
        continue;
      }
      ////
      // Expand test case set vertically.
      if (factors.isLastKey(factorName)) {
        leftOver = vg(generatedTuples, leftTuples, factors);
      } else {
        leftOver = vg(generatedTuples, leftTuples,
            factors.head(factors.nextKey(factorName)));
      }
    }
    remainderTuples.addAll(leftOver);
    return new Result(generatedTuples, remainderTuples).deduplicate();
  }

  /*
     * Returns a list of test cases in {@code generatedTuples} which are proven to be not
     * possible under given constraints.
     */
  private Set<Tuple> hg(
      List<Tuple> result, Tuples leftTuples, Factor factor) {
    Set<Tuple> leftOver = new LinkedHashSet<Tuple>();
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
        if (makeSureTupleDoesnotViolateAnyConstraintExplicitly(cur)) {
          leftTuples.removeAll(TupleUtils.subtuplesOf(cur, this.strength));
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
    // Remove empty tests from the generatedTuples.
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
      Tuple t = factors.createTupleFrom(cur, com.github.dakusui.jcunit8.core.Utils.DontCare);
      if (makeSureTupleDoesnotViolateAnyConstraintExplicitly(t)) {
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
        q.put(factorName, com.github.dakusui.jcunit8.core.Utils.DontCare);
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
      Set<Tuple> subtuplesOfBest = TupleUtils.subtuplesOf(best, this.strength);
      leftTuples.removeAll(subtuplesOfBest);
      ret.removeAll(subtuplesOfBest);
      result.add(best);
    }
    Set<Tuple> remove = new LinkedHashSet<Tuple>();
    for (Tuple testCase : result) {
      try {
        fillInMissingFactors(testCase, leftTuples);
        Set<Tuple> subtuples = TupleUtils.subtuplesOf(testCase, strength);
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
    for (Tuple invalidatedSubTuple : TupleUtils
        .subtuplesOf(tupleGivenUp, strength)) {
      if (lookup(result, invalidatedSubTuple).size() == 0) {
        ////
        // Sub-tuples that do not constraints 'explicitly' will be added
        // to 'leftOver' tuples.
        if (this.makeSureTupleDoesnotViolateAnyConstraintExplicitly(invalidatedSubTuple)) {
          leftOver.add(invalidatedSubTuple);
        }
      }
    }
  }

  /**
   * Calls an extension point in optimizer 'fillInMissingFactors'.
   * Update content of {@code tuple} using optimizer.
   * Throws a {@code GiveUp} when this method can't find a valid tuple.
   * <p/>
   * It's guaranteed that {@code tuple} doesn't violate constraints explicitly.
   * But it is possible that it can violate them as a generatedTuples of replacing "Don't care'
   * value.
   */
  protected void fillInMissingFactors(
      Tuple tuple,
      Tuples leftTuples) {
    Checks.checknotnull(tuple);
    Checks.checknotnull(leftTuples);
    Checks.checknotnull(constraintChecker);
    if (!makeSureTupleDoesnotViolateAnyConstraintExplicitly(tuple)) {
      throw new GiveUp(removeDontCareEntries(tuple));
    }
    Tuple work = this.optimizer
        .fillInMissingFactors(tuple.cloneTuple(), leftTuples,
            constraintChecker, this.factors);
    Checks.checknotnull(work);
    Checks.checkcond(work.keySet().equals(tuple.keySet()),
        "Key set was modified from %s to %s", tuple.keySet(), work.keySet());
    Checks.checkcond(!work.containsValue(com.github.dakusui.jcunit8.core.Utils.DontCare));
    if (!makeSureTupleDoesnotViolateAnyConstraintExplicitly(work)) {
      throw new GiveUp(removeDontCareEntries(work));
    }
    tuple.putAll(work);
  }

  private boolean makeSureTupleDoesnotViolateAnyConstraintExplicitly(Tuple cur) {
    Checks.checknotnull(cur);
    try {
      return constraintChecker.check(removeDontCareEntries(cur));
    } catch (UndefinedSymbol e) {
      ////
      // In case checking fails due to insufficient attribute values
      // in tuple 'cur', JCUnit considers it is 'valid'.
      // If it turns out that it violates constraints later,
      // it will be removed, but it's a separate story.
      return true;
    }
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
    Checks.checknotnull(found);
    Checks.checkcond(found.size() > 0);
    Checks.checknotnull(leftTuples);
    Checks.checknotnull(factorName);
    Tuple ret = this.optimizer
        .chooseBestTuple(found,
            leftTuples.unmodifiableVersion(), factorName, level);
    Checks.checknotnull(ret);
    Checks.checkcond(found.contains(ret),
        "User code must return a value from found tuples.");
    return ret;
  }

  /**
   * An extension point.
   * Called by 'hg' process.
   */
  protected Object chooseBestValue(String factorName, List<Object> factorLevels,
      Tuple tuple, Tuples leftTuples) {
    Checks.checknotnull(factorName);
    Checks.checknotnull(factorLevels);
    Checks.checkcond(factorLevels.size() > 0);
    Checks.checknotnull(tuple);
    Checks.checknotnull(leftTuples);

    Object ret = this.optimizer
        .chooseBestValue(factorName, factorLevels.toArray() /* By specification of 'toArray', even if the content is modified, it's safe */,
            tuple, leftTuples.unmodifiableVersion());
    Checks.checkcond(factorLevels.contains(ret));
    return ret;
  }

  private Tuple removeDontCareEntries(Tuple cur) {
    Tuple tuple = cur.cloneTuple();
    for (String factorName : cur.keySet()) {
      if (tuple.get(factorName) == com.github.dakusui.jcunit8.core.Utils.DontCare) {
        tuple.remove(factorName);
      }
    }
    return tuple;
  }
}

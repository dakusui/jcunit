package com.github.dakusui.jcunit.generators.ipo2.optimizers;

import com.github.dakusui.enumerator.tuple.CartesianEnumerator;
import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.Tuples;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.exceptions.GiveUp;
import com.github.dakusui.jcunit.exceptions.JCUnitSymbolException;
import com.github.dakusui.jcunit.generators.ipo2.IPO2;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

public class GreedyIPO2Optimizer implements IPO2Optimizer {
  private final Random random = new Random(4649);

  @Override
  public Tuple fillInMissingFactors(
      Tuple tuple,
      Tuples leftTuples,
      ConstraintManager constraintManager,
      Factors factors) {
    LinkedHashMap<String, Object[]> missingFactors = new LinkedHashMap<String, Object[]>();
    for (String f : tuple.keySet()) {
      if (tuple.get(f) == IPO2.DontCare) {
        missingFactors.put(f, factors.get(f).levels.toArray());
      }
    }
    if (missingFactors.size() == 0) {
      try {
        if (constraintManager.check(tuple)) {
          return tuple;
        }
      } catch (JCUnitSymbolException e) {
        ////
        // In case constraint checking fails for insufficient attributes, no way
        // other than moving on.
        return tuple;
      }
      throw new GiveUp(tuple);
    }
    CartesianEnumerator<String, Object> enumerator = new CartesianEnumerator<String, Object>(
        TupleUtils.map2list(
            missingFactors)
    );
    long sz = enumerator.size();
    int maxTries = Math.min(50, (int) Math.min(sz, Integer.MAX_VALUE));
    int maxNum = -1;
    Tuple ret = null;
    for (int i = 0; i < maxTries; i++) {
      long index = maxTries < sz ? i : (long) (random.nextDouble() * sz);
      Tuple t = tuple.cloneTuple();
      t.putAll(TupleUtils.list2tuple(enumerator.get(index)));
      try {
        if (!constraintManager.check(t)) {
          continue;
        }
      } catch (JCUnitSymbolException e) {
        ////
        // In case constraint checking fails for insufficient attributes, no way
        // other than moving on.
      }
      int num = leftTuples.coveredBy(t).size();
      if (num >= maxNum) {
        maxNum = num;
        ret = t;
      }
    }
    if (ret == null) {
      throw new GiveUp(tuple);
    }
    return ret;
  }

  @Override
  public Tuple chooseBestTuple(
      List<Tuple> found, Tuples leftTuples,
      String factorName, Object level) {
    int maxnum = -1;
    Tuple ret = null;
    for (Tuple t : found) {

      t.put(factorName, level);
      int num = leftTuples.coveredBy(t).size();
      if (num >= maxnum) {
        maxnum = num;
        ret = t;
      }
    }
    return ret;
  }

  @Override
  public Object chooseBestValue(String factorName,
      Object[] factorLevels, Tuple tuple,
      Tuples leftTuples) {
    int maxnum = -1;
    Object chosen = null;
    for (Object v : factorLevels) {
      tuple.put(factorName, v);
      int num = leftTuples.coveredBy(tuple).size();
      if (num >= maxnum) {
        chosen = v;
        maxnum = num;
      }
    }
    return chosen;
  }

}

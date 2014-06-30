package com.github.dakusui.jcunit.generators.ipo2.optimizers;

import com.github.dakusui.enumerator.tuple.CartesianEnumerator;
import com.github.dakusui.jcunit.constraints.ConstraintManager;
import com.github.dakusui.jcunit.core.ValueTuple;
import com.github.dakusui.jcunit.generators.ipo.GiveUp;
import com.github.dakusui.jcunit.generators.ipo2.IPO2Utils;
import com.github.dakusui.jcunit.generators.ipo2.LeftTuples;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by hiroshi on 6/30/14.
 */
public class GreedyIPO2Optimizer implements IPO2Optimizer {
  private final Random random = new Random(4649);

  @Override public ValueTuple<String, Object> fillInMissingFactors(
      ValueTuple tuple, LinkedHashMap<String, Object[]> missingFactors,
      LeftTuples leftTuples,
      ConstraintManager<String, Object> constraintManager) {
    CartesianEnumerator<String, Object> enumerator = new CartesianEnumerator<String, Object>(
        IPO2Utils.map2list(
            missingFactors)
    );
    long sz = enumerator.size();
    int maxTries = 50;
    int maxNum = -1;
    ValueTuple<String, Object> ret = null;
    for (int i = 0; i < maxTries; i++) {
      long index = maxTries < sz ? i : (long) (random.nextDouble() * sz);
      ValueTuple<String, Object> t = IPO2Utils.list2tuple(enumerator.get(index));
      t.putAll(tuple);
      if (!constraintManager.check(t))
        continue;
      int num = leftTuples.coveredBy(t).size();
      if (num > maxNum) {
        maxNum = num;
        ret = t;
      }
    }
    if (ret == null) {
      throw new GiveUp(tuple);
    }
    return ret;
  }

  @Override public ValueTuple<String, Object> chooseBestTuple(
      List<ValueTuple<String, Object>> found, LeftTuples leftTuples,
      String factorName, Object level) {
    int maxnum = -1;
    ValueTuple<String, Object> ret = null;
    for (ValueTuple<String, Object> t : found) {

      t.put(factorName, level);
      int num = leftTuples.coveredBy(t).size();
      if (num > maxnum) {
        maxnum = num;
        ret = t;
      }
    }
    return ret;
  }

  @Override public Object chooseBestValue(String factorName,
      Object[] factorLevels, ValueTuple<String, Object> tuple,
      LeftTuples leftTuples) {
    int maxnum = -1;
    Object chosen = null;
    for (Object v : factorLevels) {
      tuple.put(factorName, v);
      int num = leftTuples.coveredBy(tuple).size();
      if (num > maxnum) {
        chosen = v;
        maxnum = num;
      }
    }
    return chosen;
  }

}

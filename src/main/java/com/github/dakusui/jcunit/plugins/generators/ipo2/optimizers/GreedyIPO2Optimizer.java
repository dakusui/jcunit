package com.github.dakusui.jcunit.plugins.generators.ipo2.optimizers;

import com.github.dakusui.jcunit.plugins.constraintmanagers.ConstraintManager;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.Tuples;
import com.github.dakusui.jcunit.exceptions.GiveUp;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.generators.ipo2.IPO2;

import java.util.ArrayList;
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
    Factors.Builder missingFactorsBuilder = new Factors.Builder();
    int numMissingFactors = 0;
    for (String f : tuple.keySet()) {
      if (tuple.get(f) == IPO2.DontCare) {
        numMissingFactors++;
        missingFactorsBuilder.add(factors.get(f));
      }
    }
    if (numMissingFactors == 0) {
      return tuple;
    }
    Factors missingFactors = missingFactorsBuilder.build();

    int maxTries = 50;
    int maxNum = -1;
    List<Tuple> candidates = new ArrayList<Tuple>(maxTries);
    for (int i = 0; i < maxTries; i++) {
      Tuple t = creteRandomTuple(missingFactors, tuple);
      try {
        if (!constraintManager.check(t)) {
          continue;
        }
      } catch (UndefinedSymbol e) {
        ////
        // In case constraint checking fails for insufficient attributes, no way
        // other than moving on.
      }
      int num = leftTuples.coveredBy(t).size();
      if (num >= maxNum) {
        if (num > maxNum) {
          candidates.clear();
        }
        maxNum = num;
        candidates.add(t);
      }
    }
    if (candidates.isEmpty()) {
      throw new GiveUp(tuple);
    }
    Tuple ret;
    ret = candidates.get(this.random.nextInt(candidates.size()));
    return ret;
  }

  @Override
  public Tuple chooseBestTuple(
      List<Tuple> found, Tuples leftTuples,
      String factorName, Object level) {
    int maxnum = -1;
    List<Tuple> candidates = new ArrayList<Tuple>(found.size());
    for (Tuple t : found) {
      t.put(factorName, level);
      int num = leftTuples.coveredBy(t).size();
      if (num >= maxnum) {
        if (num > maxnum) {
          candidates.clear();
        }
        maxnum = num;
        candidates.add(t);
      }
    }
    Tuple ret;
    ret = found.get(this.random.nextInt(candidates.size()));
    return ret;
  }

  @Override
  public Object chooseBestValue(String factorName,
      Object[] factorLevels, Tuple tuple,
      Tuples leftTuples) {
    int maxnum = -1;
    List<Object> candidates = new ArrayList<Object>();
    for (Object v : factorLevels) {
      tuple.put(factorName, v);
      int num = leftTuples.coveredBy(tuple).size();
      if (num >= maxnum) {
        if (num > maxnum) {
          candidates.clear();
        }
        candidates.add(v);
        maxnum = num;
      }
    }
    Object chosen;
    chosen = candidates.get(this.random.nextInt(candidates.size()));
    return chosen;
  }

  private Tuple creteRandomTuple(Factors missingFactors, Tuple base) {
    Tuple ret = base.cloneTuple();
    for (String fn : missingFactors.getFactorNames()) {
      List<Object> levels = missingFactors.get(fn).levels;
      int index = this.random.nextInt(levels.size());
      ret.put(fn, levels.get(index));
    }
    return ret;
  }
}

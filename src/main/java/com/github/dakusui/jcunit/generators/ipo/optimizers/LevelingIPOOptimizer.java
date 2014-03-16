package com.github.dakusui.jcunit.generators.ipo.optimizers;

import com.github.dakusui.jcunit.generators.ipo.IPO;
import com.github.dakusui.jcunit.generators.ipo.TestRun;
import com.github.dakusui.jcunit.generators.ipo.TestRunSet;
import com.github.dakusui.jcunit.generators.ipo.TestSpace;

public class LevelingIPOOptimizer extends IPOOptimizer {

  private int c;

  public LevelingIPOOptimizer(TestSpace space) {
    super(space);
    this.c = 0;
  }

  @Override
  protected Object bestValueFor(TestRunSet currentTestRunSet, TestRun testRun,
      int fieldId) {
    // //
    // In order to make the test run set the same any time (reproducible), we
    // avoid using 'entrySet', which doesn't guarantee the result will be always
    // the same.
    int numUsed = Integer.MAX_VALUE;
    Object ret = IPO.DC;
    if (currentTestRunSet.width() >= fieldId) {
      for (Object v : this.hgCandidates) {
        int c = 0;
        for (TestRun cur : currentTestRunSet) {
          if (v == cur.get(fieldId)) {
            c++;
          }
        }
        if (c <= numUsed) {
          ret = v;
          numUsed = c;
        }
      }
    } else {
      ret = this.hgCandidates.get(c % this.hgCandidates.size());
      c++;
    }
    return ret;
  }

  @Override
  public Object optimizeInVG(TestRunSet currentTestRunSet, TestRun testRun,
      int fieldId) {
    Object[] values = this.space.domainOf(fieldId);
    int numUsed = Integer.MAX_VALUE;
    Object ret = IPO.DC;
    for (Object v : values) {
      int c = 0;
      for (TestRun cur : currentTestRunSet) {
        if (v == cur.get(fieldId)) {
          c++;
        }
      }
      if (c <= numUsed) {
        ret = v;
        numUsed = c;
      }
    }
    return ret;
  }

  @Override
  public TestRunSet createTestRunSet(int width) {
    return new TestRunSet(width);
  }
}

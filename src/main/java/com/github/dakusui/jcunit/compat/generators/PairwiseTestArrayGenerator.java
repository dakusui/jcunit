package com.github.dakusui.jcunit.compat.generators;

import com.github.dakusui.jcunit.compat.generators.ipo.IPO;
import com.github.dakusui.jcunit.compat.generators.ipo.IPOTestRun;
import com.github.dakusui.jcunit.compat.generators.ipo.IPOTestRunSet;
import com.github.dakusui.jcunit.compat.generators.ipo.IPOTestSpace;
import com.github.dakusui.jcunit.compat.generators.ipo.optimizers.GreedyIPOOptimizer;
import com.github.dakusui.jcunit.compat.core.annotations.GeneratorParameters;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class doesn't guarantee that the generated covering array is the
 * smallest one.
 *
 * @param <T> Type of keys
 * @author hiroshi
 * @see BaseTestArrayGenerator
 * @see TestArrayGenerator
 */
public class PairwiseTestArrayGenerator<T> extends
    BaseTestArrayGenerator<T> {
  /**
   * A set of test runs.
   */
  private IPOTestRunSet testRunSet;

  /**
   * A map which associates IPO's indices (1-origin) to keys.
   */
  private Map<Integer, T> indexToKeyMap = new HashMap<Integer, T>();

  @Override
  protected long initializeTestCases(GeneratorParameters.Value[] params,
      LinkedHashMap<T, Object[]> domains) {
    super.init(params, domains);
    this.testRunSet = this.composeTestRunSet(indexToKeyMap);
    return this.testRunSet.size();
  }

  /**
   * Composes and returns test run set.
   */
  protected IPOTestRunSet composeTestRunSet(Map<Integer, T> indexToKeyMap) {
    Object[][] testSpaceDomains = new Object[this.domains.size()][];
    int i = 0;
    for (T cur : this.domains.keySet()) {
      testSpaceDomains[i++] = this.domains.get(cur);
      indexToKeyMap.put(i, cur); // since i is already incremented, put it as
      // is.
    }
    IPOTestSpace space = new IPOTestSpace(testSpaceDomains);
    IPO ipo = new IPO(space, new GreedyIPOOptimizer(space));
    return ipo.ipo();
  }

  @SuppressWarnings("unchecked")
  @Override
  public int getIndex(T key, long cur) {
    IPOTestRun run = this.testRunSet.get((int) cur);
    // IPO classes provide 1-origin methods!
    for (int i = 1; i <= run.width(); i++) {
      T k = indexToKeyMap.get(i);
      if (key.equals(k)) {
        Object[] domainOf_i = findDomain(k);
        return indexOf(run.get(i), domainOf_i);
      }
    }
    assert false;
    return -1;
  }

  private Object[] findDomain(T key) {
    return this.domains.get(key);
  }

  /*
     * returns an index of specified value by using '==' operator not by using
     * 'equals' method.
     */
  private int indexOf(Object u, Object[] domain) {
    int i = 0;
    for (Object obj : domain) {
      if (obj == u) {
        return i;
      }
      i++;
    }
    return -1;
  }
}

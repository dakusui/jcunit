package com.github.dakusui.jcunit.generators;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.github.dakusui.jcunit.core.GeneratorParameters;
import com.github.dakusui.jcunit.generators.ipo.IPO;
import com.github.dakusui.jcunit.generators.ipo.IPOTestRun;
import com.github.dakusui.jcunit.generators.ipo.IPOTestRunSet;
import com.github.dakusui.jcunit.generators.ipo.IPOTestSpace;
import com.github.dakusui.jcunit.generators.ipo.optimizers.GreedyIPOOptimizer;

/**
 * This class doesn't guarantee that the generated covering array is the
 * smallest one.
 * 
 * @see BaseTestArrayGenerator
 * @see TestArrayGenerator
 * 
 * @author hiroshi
 * 
 * @param <T>
 *          Type of keys
 * @param <U>
 *          Type of values
 */
public class PairwiseTestArrayGenerator<T, U> extends
    BaseTestArrayGenerator<T, U> {
  /**
   * A set of test runs.
   */
  private IPOTestRunSet      testRunSet;

  /**
   * A map which associates IPO's indices (1-origin) to keys.
   */
  private Map<Integer, T> indexToKeyMap = new HashMap<Integer, T>();

  @Override
  public void init(GeneratorParameters.Value[] params,
      LinkedHashMap<T, U[]> domains) {
    super.init(params, domains);
    this.testRunSet = this.composeTestRunSet(indexToKeyMap);
    this.size = this.testRunSet.size();
    this.cur = 0;
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
        U[] domainOf_i = findDomain(k);
        return indexOf((U) run.get(i), domainOf_i);
      }
    }
    assert false;
    return -1;
  }

  private U[] findDomain(T key) {
    return this.domains.get(key);
  }

  /*
   * returns an index of specified value by using '==' operator not by using
   * 'equals' method.
   */
  private int indexOf(U u, U[] domain) {
    int i = 0;
    for (Object obj : domain) {
      if (obj == u)
        return i;
      i++;
    }
    return -1;
  }
}

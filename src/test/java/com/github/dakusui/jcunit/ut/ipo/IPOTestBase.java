package com.github.dakusui.jcunit.ut.ipo;

import com.github.dakusui.enumerator.Combinator;
import com.github.dakusui.jcunit.compat.generators.ipo.IPO;
import com.github.dakusui.jcunit.compat.generators.ipo.IPOTestRunSet;
import com.github.dakusui.jcunit.compat.generators.ipo.IPOTestSpace;
import com.github.dakusui.jcunit.compat.generators.ipo.IPOValuePair;
import com.github.dakusui.jcunit.compat.generators.ipo.optimizers.IPOOptimizer;
import com.github.dakusui.jcunit.testutils.TestSettings;

import java.util.*;

public abstract class IPOTestBase {
  public static class Task {
    int numFactors;
    int numLevels;

    public Task(int numLevels, int numFactors) {
      this.numLevels = numLevels;
      this.numFactors = numFactors;
    }

    public int numFactors() {
      return this.numFactors;
    }

    public int numLevels() {
      return this.numLevels;
    }

  }

  private static IPOTestSpace createRandomizedTestSpace(Task... tasks) {
    List<Object[]> work = composeTestDomain(tasks);
    Collections.shuffle(work);
    IPOTestSpace ret = new IPOTestSpace(work.toArray(new Object[][] { }));
    return ret;
  }

  protected static List<Object[]> composeTestDomain(Task... tasks) {
    List<Object[]> work = new LinkedList<Object[]>();
    int d = 0;
    for (Task task : tasks) {
      for (int i = 0; i < task.numFactors; i++) {
        Object[] tmp = new Object[task.numLevels];
        for (int j = 0; j < tmp.length; j++) {
          tmp[j] = new Character((char) ('A' + d)).toString() + j;
        }
        d++;
        work.add(tmp);
      }
    }
    return work;
  }

  protected static IPOTestSpace createTestSpace(Task... tasks) {
    List<Object[]> work = composeTestDomain(tasks);
    return new IPOTestSpace(work.toArray(new Object[][] { }));
  }

  protected static Task task(int numLevels, int numFactors) {
    return new Task(numLevels, numFactors);
  }

  private   IPO               ipo;
  private   IPOOptimizer      optimizer;
  private   IPOTestSpace      testSpace;
  protected IPOTestRunSet     testRunSet;
  protected Set<IPOValuePair> allPairs;

  protected IPO createIPO(IPOTestSpace space, IPOOptimizer optimizer) {
    return new IPO(space, createOptimizer(space));
  }

  abstract protected IPOOptimizer createOptimizer(IPOTestSpace space);

  protected void performIPO(Task... tasks) {
    this.testRunSet = null;
    this.ipo = null;
    this.optimizer = null;
    this.testSpace = null;

    this.testSpace = createTestSpace(tasks);
    this.allPairs = createAllPairs(this.testSpace);
    this.optimizer = createOptimizer(this.testSpace);
    this.ipo = createIPO(this.testSpace, this.optimizer);
    this.testRunSet = this.ipo.ipo();
  }

  private Set<IPOValuePair> createAllPairs(IPOTestSpace testSpace) {
    if (!TestSettings.isCoveringCheckEnabled()) {
      return new HashSet<IPOValuePair>();
    }
    Set<IPOValuePair> ret = new HashSet<IPOValuePair>();
    List<Integer> domains = new ArrayList<Integer>();
    // domain index is 1-origin.
    for (int i = 1; i <= testSpace.domains().length; i++) {
      domains.add(i);
    }
    Combinator<Integer> comb = new Combinator<Integer>(domains, 2);
    for (List<Integer> c : comb) {
      for (Object r : testSpace.domainOf(c.get(0))) {
        for (Object s : testSpace.domainOf(c.get(1))) {
          IPOValuePair p = new IPOValuePair(c.get(0), r, c.get(1), s);
          ret.add(p);
        }
      }
    }
    return ret;
  }

  protected int performTestRunSetCreationRepeatedlyWithRandomness(int times,
      Task... tasks) {
    int min = Integer.MAX_VALUE;
    for (int i = 0; i < times; i++) {
      IPOTestSpace testSpace = createRandomizedTestSpace(tasks);
      IPOTestRunSet testRunSet = new IPO(testSpace, createOptimizer(testSpace))
          .ipo();
      int cur = testRunSet.size();
      if (cur < min) {
        min = cur;
      }
      printTestRunSet();
    }
    System.out.println("---");
    return min;
  }

  /**
   * @param testRunSet
   */
  protected void printTestRunSet() {
    System.out.println(String.format("%d\t%d\t%d", this.testRunSet.size(),
        testRunSet.getInfo().numHorizontalFallbacks,
        testRunSet.getInfo().numVerticalFallbacks));
  }

}

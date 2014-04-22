package com.github.dakusui.jcunit.ut.ipo;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.github.dakusui.jcunit.generators.ipo.IPOValuePair;
import com.github.dakusui.jcunit.testutils.TestSettings;

public abstract class IPOExamples extends IPOTestBase {
  private static final List<IPOValuePair> EMPTY_LIST = new LinkedList<IPOValuePair>();

  protected abstract int expected01$3_3();

  protected abstract int expected02$3_13();

  protected abstract int expected03$4_15$3_17$2_20();

  protected abstract int expected04$4_1$3_30$2_35();

  protected abstract int expected05$2_100();

  protected abstract int expected06$10_20();

  @Test
  public void test01$3_3() {
    Task[] tasks = new Task[] { task(3, 3) };
    performIPO(tasks);
    printTestRunSet();
    assertEquals(EMPTY_LIST, pairsNotCovered());
    assertEquals(expected01$3_3(), testRunSet.size());
  }

  @Test
  public void test02$3_13() {
    performIPO(task(3, 13));
    printTestRunSet();
    assertEquals(EMPTY_LIST, pairsNotCovered());
    assertEquals(expected02$3_13(), testRunSet.size());
  }

  @Test
  public void test03$4_15$3_17$2_20() {
    performIPO(task(4, 15), task(3, 17), task(2, 20));
    printTestRunSet();
    assertEquals(EMPTY_LIST, pairsNotCovered());
    assertEquals(expected03$4_15$3_17$2_20(), testRunSet.size());
  }

  @Test
  public void test04$4_1$3_30$2_35() {
    performIPO(task(4, 1), task(3, 35), task(2, 35));
    printTestRunSet();
    assertEquals(EMPTY_LIST, pairsNotCovered());
    assertEquals(expected04$4_1$3_30$2_35(), testRunSet.size());
  }

  @Test
  public void test05$2_100() {
    performIPO(task(2, 100));
    printTestRunSet();
    assertEquals(EMPTY_LIST, pairsNotCovered());
    assertEquals(expected05$2_100(), testRunSet.size());
  }

  @Test
  public void test06$10_20() {
    performIPO(task(10, 20));
    printTestRunSet();
    assertEquals(EMPTY_LIST, pairsNotCovered());
    assertEquals(expected06$10_20(), testRunSet.size());
  }

  private List<IPOValuePair> pairsNotCovered() {
    if (!TestSettings.isCoveringCheckEnabled()) {
      return EMPTY_LIST;
    }
    System.out.println("All pairs:<" + this.allPairs + ">");
    List<IPOValuePair> pairsNotFound = new LinkedList<IPOValuePair>();
    for (IPOValuePair p : this.allPairs) {
      if (!testRunSet.covers(p)) {
        pairsNotFound.add(p);
      }
    }
    System.out.println(String.format("%d out of %d pairs not covered.",
        pairsNotFound.size(), this.allPairs.size()));
    return pairsNotFound;
  }
}

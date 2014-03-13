package com.github.dakusui.jcunit.ut.ipo;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import com.github.dakusui.jcunit.generators.ipo.IPO;
import com.github.dakusui.jcunit.generators.ipo.TestRunSet;
import com.github.dakusui.jcunit.generators.ipo.TestSpace;
import com.github.dakusui.jcunit.generators.ipo.ValueTuple.Attr;
import com.github.dakusui.jcunit.generators.ipo.ValueTuple.ValueTriple;

public class IPOTest {
  public static class Task {
    int numLevels;
    int numFactors;

    public Task(int numLevels, int numFactors) {
      this.numLevels = numLevels;
      this.numFactors = numFactors;
    }

    public int numLevels() {
      return this.numLevels;
    }

    public int numFactors() {
      return this.numFactors;
    }

  }

  private static Task task(int numLevels, int numFactors) {
    return new Task(numLevels, numFactors);
  }

  private static TestSpace createTestSpace(Task... tasks) {
    List<Object[]> work = composeTestDomain(tasks);
    return new TestSpace(work.toArray(new Object[][] {}));
  }

  private static TestSpace createRandomizedTestSpace(Task... tasks) {
    List<Object[]> work = composeTestDomain(tasks);
    Collections.shuffle(work);
    TestSpace ret = new TestSpace(work.toArray(new Object[][] {}));
    return ret;
  }

  private static List<Object[]> composeTestDomain(Task... tasks) {
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

  @Test
  public void test3_3() {
    TestRunSet testRunSet = createTestRunSet(task(3, 3));
    printTestRunSet(testRunSet);
    assertEquals(9, testRunSet.size());
  }

  @Test
  public void test3_13() {
    TestRunSet testRunSet = createTestRunSet(task(3, 13));
    printTestRunSet(testRunSet);
    assertEquals(27, testRunSet.size());
  }

  @Test(timeout = 600 * 1000)
  public void test4_15$3_17$2_20() {
    TestRunSet testRunSet = createTestRunSet(task(4, 15), task(3, 17),
        task(2, 20));
    printTestRunSet(testRunSet);
    assertEquals(73, testRunSet.size());
  }

  @Test
  public void testComposingTestDomain() {
    for (Object[] row : composeTestDomain(task(4, 15), task(3, 17), task(2, 20))) {
      System.out.println(ArrayUtils.toString(row));
    }
  }

  @Test(timeout = 60 * 1000)
  public void test4_1$3_20$2_35() {
    assertEquals(74,
        new IPO(createTestSpace(task(4, 15), task(3, 17), task(2, 35))).ipo()
            .size());
  }

  @Test
  public void test2_100() {
    assertEquals(22, new IPO(createTestSpace(task(2, 100))).ipo().size());
  }

  @Test
  public void test10_20() {
    assertEquals(286, new IPO(createTestSpace(task(10, 20))).ipo().size());
  }

  @Test
  public void test4_1$3_20$2_35_random() {
    TestSpace testSpace = createRandomizedTestSpace(task(4, 15), task(3, 17),
        task(2, 35));
    System.out.println("----");
    int min = performTestRunSetCreationRepeatedlyWithRandomness(testSpace, 10);
    System.out.println(min);
  }

  @Test
  public void test4_1$3_20$2_20_random() {
    TestSpace testSpace = createRandomizedTestSpace(task(4, 15), task(3, 17),
        task(2, 20));
    System.out.println("----");
    int min = performTestRunSetCreationRepeatedlyWithRandomness(testSpace, 10);
    System.out.println(min);
  }

  @Test
  public void test3() {
    Task[] tasks = new Task[] { task(3, 2), task(4, 1), task(5, 1) };
    TestRunSet testRunSet = createTestRunSet(tasks);

    printTestRunSet(testRunSet);
  }

  /**
   * @param testRunSet
   */
  protected void printTestRunSet(TestRunSet testRunSet) {
    System.out.println(String.format("%d\t%d\t%d", testRunSet.size(),
        testRunSet.getInfo().numHorizontalFallbacks,
        testRunSet.getInfo().numVerticalFallbacks));
  }

  /**
   * @param tasks
   * @return
   */
  protected TestRunSet createTestRunSet(Task... tasks) {
    TestRunSet testRunSet = new IPO(createTestSpace(tasks)).ipo();
    return testRunSet;
  }

  @Test
  public void testTriple() {
    Set<ValueTriple> triples = new HashSet<ValueTriple>();
    triples.add(new ValueTriple(new Attr(1, "X"), new Attr(2, "Y"), new Attr(3,
        "Z")));
    System.out.println(">>" + triples);
    System.out.println(">>"
        + triples.contains(new ValueTriple(new Attr(1, "X"), new Attr(2, "Y"),
            new Attr(3, "Z"))));
    System.out.println(">>"
        + triples.contains(new ValueTriple(new Attr(1, "X"), new Attr(2, "Y"),
            new Attr(3, "W"))));
  }

  private int performTestRunSetCreationRepeatedlyWithRandomness(
      TestSpace testSpace, int times) {
    int min = Integer.MAX_VALUE;
    for (int i = 0; i < times; i++) {
      TestRunSet testRunSet = new IPO(testSpace).ipo();
      int cur = testRunSet.size();
      if (cur < min)
        min = cur;
      printTestRunSet(testRunSet);
    }
    System.out.println("---");
    return min;
  }
}

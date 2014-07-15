package com.github.dakusui.jcunit.extras.generators.ipo;

import com.github.dakusui.jcunit.compat.generators.ipo.IPOTestSpace;
import com.github.dakusui.jcunit.compat.generators.ipo.IPOValueTuple.Attr;
import com.github.dakusui.jcunit.compat.generators.ipo.IPOValueTuple.ValueTriple;
import com.github.dakusui.jcunit.compat.generators.ipo.optimizers.IPOOptimizer;
import com.github.dakusui.jcunit.compat.generators.ipo.optimizers.LevelingIPOOptimizer;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class IPOTestSandbox extends IPOTestBase {

  @Test
  public void test4_15$3_17$2_20_random() {
    System.out.println("----");
    int min = performTestRunSetCreationRepeatedlyWithRandomness(100,
        task(4, 15), task(3, 17), task(2, 20));
    System.out.println(min);
  }

  @Test
  public void test4_1$3_30$2_35_random() {
    System.out.println("----");
    int min = performTestRunSetCreationRepeatedlyWithRandomness(10, task(4, 1),
        task(3, 30), task(2, 35));
    System.out.println(min);
  }

  @Test
  public void test3() {
    Task[] tasks = new Task[] { task(3, 2), task(4, 1), task(5, 1) };
    performIPO(tasks);

    printTestRunSet();
  }

  @Test
  public void testComposingTestDomain() {
    for (Object[] row : composeTestDomain(task(4, 15), task(3, 17),
        task(2, 20))) {
      System.out.println(ArrayUtils.toString(row));
    }
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

  @Override
  protected IPOOptimizer createOptimizer(IPOTestSpace space) {
    return new LevelingIPOOptimizer(space);
  }
}

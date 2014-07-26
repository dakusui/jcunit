package com.github.dakusui.jcunit.framework.tests.ipo2;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.generators.ipo2.IPO2;
import com.github.dakusui.jcunit.generators.ipo2.optimizers.IPO2Optimizer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class StabilityTest extends IPO2Test {
  List<Tuple> generateTestCases() {
    Factors factors = buildFactors(
        factorsDef(2, 1),
        factorsDef(4, 2),
        factorsDef(4, 2),
        factorsDef(2, 2),
        factorsDef(2, 1),
        factorsDef(5, 2));
    ConstraintManager constraintManager = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = generateIPO2(factors, 2, constraintManager, optimizer);
    return ipo.getResult();
  }

  @Test
  public void test() {
    int n = 10;
    List<List<Tuple>> resultsList = new ArrayList<List<Tuple>>(10);
    for (int i = 0; i < n; i++) {
      List<Tuple> testCases = generateTestCases();
      resultsList.add(testCases);
    }
    boolean allPassed = true;
    for (int i = 0; i < n; i++) {
      System.out.print(String.format("%4d ", resultsList.get(i).size()));
      for (int j = 0; j < i; j++) {
        if (!resultsList.get(i).equals(resultsList.get(0))) {
          System.out.print("NG ");
          allPassed = false;
        } else {
          System.out.print("OK ");
        }
      }
      System.out.println();
      assertTrue(allPassed);
    }
  }
}

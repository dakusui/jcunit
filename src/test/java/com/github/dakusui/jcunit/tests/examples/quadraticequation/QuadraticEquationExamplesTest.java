package com.github.dakusui.jcunit.tests.examples.quadraticequation;

import com.github.dakusui.jcunit.examples.quadraticequation.session1.QuadraticEquationSolverTest1;
import com.github.dakusui.jcunit.examples.quadraticequation.session2.QuadraticEquationSolverTest2;
import com.github.dakusui.jcunit.examples.quadraticequation.session3.QuadraticEquationSolverTest3;
import com.github.dakusui.jcunit.examples.quadraticequation.session4.QuadraticEquationSolverTest4;
import com.github.dakusui.jcunit.examples.quadraticequation.session5.QuadraticEquationSolverTest5;
import com.github.dakusui.jcunit.examples.quadraticequation.session6.QuadraticEquationSolverTest6;
import com.github.dakusui.jcunit.testutils.UTUtils;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import static org.junit.Assert.assertEquals;

public class QuadraticEquationExamplesTest {

  @Test
  public void verifyTest1() {
    Result result = JUnitCore.runClasses(QuadraticEquationSolverTest1.class);
    assertEquals(QuadraticEquationSolverTest1.failureCount, result.getFailureCount());
    assertEquals(QuadraticEquationSolverTest1.runCount, result.getRunCount());
    assertEquals(QuadraticEquationSolverTest1.ignoreCount, result.getIgnoreCount());
  }

  @Test
  public void verifyTest2() {
    Result result = JUnitCore.runClasses(QuadraticEquationSolverTest2.class);
    assertEquals(QuadraticEquationSolverTest2.failureCount, result.getFailureCount());
    assertEquals(QuadraticEquationSolverTest2.runCount, result.getRunCount());
    assertEquals(QuadraticEquationSolverTest2.ignoreCount, result.getIgnoreCount());
  }

  @Test
  public void verifyTest3() {
    Result result = JUnitCore.runClasses(QuadraticEquationSolverTest3.class);
    assertEquals(QuadraticEquationSolverTest3.failureCount, result.getFailureCount());
    assertEquals(QuadraticEquationSolverTest3.runCount, result.getRunCount());
    assertEquals(QuadraticEquationSolverTest3.ignoreCount, result.getIgnoreCount());
  }

  @Test
  public void verifyTest4() {
    QuadraticEquationSolverTest4.ps = UTUtils.DUMMY_PRINTSTREAM;
    Result result = JUnitCore.runClasses(QuadraticEquationSolverTest4.class);
    assertEquals(QuadraticEquationSolverTest4.failureCount, result.getFailureCount());
    assertEquals(QuadraticEquationSolverTest4.runCount, result.getRunCount());
    assertEquals(QuadraticEquationSolverTest4.ignoreCount, result.getIgnoreCount());
  }

  @Test
  public void verifyTest5() {
    QuadraticEquationSolverTest5.ps = UTUtils.DUMMY_PRINTSTREAM;
    Result result = JUnitCore.runClasses(QuadraticEquationSolverTest5.class);
    assertEquals(QuadraticEquationSolverTest5.failureCount, result.getFailureCount());
    assertEquals(QuadraticEquationSolverTest5.runCount, result.getRunCount());
    assertEquals(QuadraticEquationSolverTest5.ignoreCount, result.getIgnoreCount());
  }

  @Test
  public void verifyTest6() {
    QuadraticEquationSolverTest6.ps = UTUtils.DUMMY_PRINTSTREAM;
    Result result = JUnitCore.runClasses(QuadraticEquationSolverTest6.class);
    assertEquals(QuadraticEquationSolverTest6.failureCount, result.getFailureCount());
    assertEquals(QuadraticEquationSolverTest6.runCount, result.getRunCount());
    assertEquals(QuadraticEquationSolverTest6.ignoreCount, result.getIgnoreCount());
  }
}

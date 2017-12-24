package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

import java.util.function.Function;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class IncrementalJoinExperimentBase {
  @Rule
  public TestName testName = new TestName();

  @BeforeClass
  public static void warmup() {
    runJoin("(warmup)", 2, 10, 10, JoinSession::standard);
  }

  @Test
  public void joinLhs10_Rhs10() {
    runJoin(testName.getMethodName(), doi(), 10, 10, joinerFactory());
  }

  @Test
  public void joinLhs20_Rhs10() {
    runJoin(testName.getMethodName(), doi(), 20, 10, joinerFactory());
  }

  @Test
  public void joinLhs30_Rhs10() {
    runJoin(testName.getMethodName(), doi(), 30, 10, joinerFactory());
  }

  @Test
  public void joinLhs40_Rhs10() {
    runJoin(testName.getMethodName(), doi(), 40, 10, joinerFactory());
  }

  @Test
  public void joinLhs50_Rhs10() {
    runJoin(testName.getMethodName(), doi(), 50, 10, joinerFactory());
  }

  @Test
  public void joinLhs60_Rhs10() {
    runJoin(testName.getMethodName(), doi(), 60, 10, joinerFactory());
  }

  @Test
  public void joinLhs70_Rhs10() {
    runJoin(testName.getMethodName(), doi(), 70, 10, joinerFactory());
  }

  @Test
  public void joinLhs80_Rhs10() {
    runJoin(testName.getMethodName(), doi(), 80, 10, joinerFactory());
  }

  @Test
  public void joinLhs90_Rhs10() {
    runJoin(testName.getMethodName(), doi(), 90, 10, joinerFactory());
  }

  @Test
  public void joinLhs100_Rhs10() {
    runJoin(testName.getMethodName(), doi(), 100, 10, joinerFactory());
  }

  abstract Function<Requirement, Joiner> joinerFactory();
  abstract int doi();

  private static void runJoin(String testName, int doi, int lhsNumFactors, int rhsNumFactors, Function<Requirement, Joiner> joinerFactory) {
    SchemafulTupleSet lhs = JoinDataSet.load(doi, lhsNumFactors);
    SchemafulTupleSet rhs = JoinDataSet.load(doi, rhsNumFactors, integer -> String.format("r%03d", integer));
    JoinSession session = new JoinSession.Builder(doi).with(joinerFactory).lhs(lhs).rhs(rhs).build();
    session.execute();
    System.out.printf("%s: size=%d; width=%d; time=%s[msec]; %s%n", testName, session.result.size(), session.result.width(), session.time(), session.result.getAttributeNames().size());
    //    session.result.forEach(System.out::println);
  }
}

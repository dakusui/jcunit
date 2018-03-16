package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.pipeline.stages.joiners.StandardJoiner;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.function.Function;

import static com.github.dakusui.jcunit8.experiments.FlorenceJoinExperiment.runJoin;

@RunWith(Enclosed.class)
public class IncrementalJoinExperiment {
  public static class WithStandardJoiner_Doi2 extends TestBase {
    Function<Requirement, Joiner> joinerFactory() {
      return JoinSession::standard;
    }

    @Override
    int doi() {
      return 2;
    }
  }

  public static class WithIncrementalJoiner_Doi2 extends TestBase {
    Function<Requirement, Joiner> joinerFactory() {
      return JoinSession::incremental;
    }

    @Override
    int doi() {
      return 2;
    }
  }

  public static class WithIncrementalJoiner_Doi3 extends TestBase {
    Function<Requirement, Joiner> joinerFactory() {
      return JoinSession::incremental;
    }

    @Override
    int doi() {
      return 3;
    }
  }

  @FixMethodOrder(MethodSorters.NAME_ASCENDING)
  public abstract static class TestBase {
    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void warmup() {
      if (!StandardJoiner.isDebugEnabled()) {
        runJoin("(warmup)", 2, 10, 10, JoinSession::standard, false);
      }
    }

    @Test
    public void joinLhs010_Rhs10() {
      runJoin(testName.getMethodName(), doi(), 10, 10, joinerFactory(), false);
    }

    @Test
    public void joinLhs020_Rhs10() {
      runJoin(testName.getMethodName(), doi(), 20, 10, joinerFactory(), false);
    }

    @Test
    public void joinLhs030_Rhs10() {
      runJoin(testName.getMethodName(), doi(), 30, 10, joinerFactory(), false);
    }

    @Test
    public void joinLhs040_Rhs10() {
      runJoin(testName.getMethodName(), doi(), 40, 10, joinerFactory(), false);
    }

    @Test
    public void joinLhs050_Rhs10() {
      runJoin(testName.getMethodName(), doi(), 50, 10, joinerFactory(), false);
    }

    @Test
    public void joinLhs060_Rhs10() {
      runJoin(testName.getMethodName(), doi(), 60, 10, joinerFactory(), false);
    }

    @Test
    public void joinLhs070_Rhs10() {
      runJoin(testName.getMethodName(), doi(), 70, 10, joinerFactory(), false);
    }

    @Test
    public void joinLhs080_Rhs10() {
      runJoin(testName.getMethodName(), doi(), 80, 10, joinerFactory(), false);
    }

    @Test
    public void joinLhs090_Rhs10() {
      runJoin(testName.getMethodName(), doi(), 90, 10, joinerFactory(), false);
    }

    @Test
    public void joinLhs100_Rhs10() {
      runJoin(testName.getMethodName(), doi(), 100, 10, joinerFactory(), false);
    }

    abstract Function<Requirement, Joiner> joinerFactory();

    abstract int doi();

  }
}


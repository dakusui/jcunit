package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.pipeline.stages.joiners.StandardJoiner;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.function.Function;

@RunWith(Enclosed.class)
public class FlorenceJoinExperiment {
  public static class WithStandardJoiner_Doi2 extends TestBase {
    Function<Requirement, Joiner> joinerFactory() {
      return JoinSession::standard;
    }

    @Override
    int doi() {
      return 2;
    }
  }

  public static class WithFlorenceJoiner_Doi2 extends TestBase {
    Function<Requirement, Joiner> joinerFactory() {
      return JoinSession::florence;
    }

    @Override
    int doi() {
      return 2;
    }
  }

  public static class WithFlorenceJoiner_Doi3 extends TestBase {
    Function<Requirement, Joiner> joinerFactory() {
      return JoinSession::florence;
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
        runJoin("(warmup)", 2, 10, 10, JoinSession::florence, true);
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

  static void runJoin(String testName, int doi, int lhsNumFactors, int rhsNumFactors, Function<Requirement, Joiner> joinerFactory, boolean fullCheck) {
    SchemafulTupleSet lhs = JoinDataSet.load(doi, lhsNumFactors);
    SchemafulTupleSet rhs = JoinDataSet.load(doi, rhsNumFactors, integer -> String.format("r%03d", integer));
    JoinSession session = new JoinSession.Builder(doi).with(joinerFactory).lhs(lhs).rhs(rhs).fullCheck(fullCheck).build();
    session.execute();
    session.verify();
    System.out.printf("%s: size=%d; width=%d; time=%s[msec]; %s (input=lhs=%s; rhs=%s)%n",
        testName, session.result.size(), session.result.width(),
        session.time(), session.result.getAttributeNames().size(),
        lhs.size(),
        rhs.size()
    );
  }
}


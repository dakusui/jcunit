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
public class LucasJoinExperiment {

  public static class WithLucasJoiner_Doi2_Rhs20 extends TestBase {
    Function<Requirement, Joiner> joinerFactory() {
      return JoinSession::lucas;
    }

    @Override
    int doi() {
      return 2;
    }

    @Override
    int rhsWidth() {
      return 20;
    }
  }

  public static class WithLucasJoiner_Doi2_Rhs30 extends TestBase {
    Function<Requirement, Joiner> joinerFactory() {
      return JoinSession::lucas;
    }

    @Override
    int doi() {
      return 2;
    }

    @Override
    int rhsWidth() {
      return 30;
    }
  }

  public static class WithLucasJoiner_Doi2_Rhs40 extends TestBase {
    Function<Requirement, Joiner> joinerFactory() {
      return JoinSession::lucas;
    }

    @Override
    int doi() {
      return 2;
    }

    @Override
    int rhsWidth() {
      return 40;
    }
  }

  public static class WithLucasJoiner_Doi2_Rhs50 extends TestBase {
    Function<Requirement, Joiner> joinerFactory() {
      return JoinSession::lucas;
    }

    @Override
    int doi() {
      return 2;
    }

    @Override
    int rhsWidth() {
      return 50;
    }
  }

  public static class WithLucasJoiner_Doi2 extends TestBase {
    Function<Requirement, Joiner> joinerFactory() {
      return JoinSession::lucas;
    }

    @Override
    int doi() {
      return 2;
    }

    @Override
    int rhsWidth() {
      return 10;
    }
  }

  public static class WithLucasJoiner_Doi3 extends TestBase {
    Function<Requirement, Joiner> joinerFactory() {
      return JoinSession::lucas;
    }

    @Override
    int doi() {
      return 3;
    }

    @Override
    int rhsWidth() {
      return 10;
    }
  }

  @FixMethodOrder(MethodSorters.NAME_ASCENDING)
  public abstract static class TestBase {
    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void warmup() {
      if (!StandardJoiner.isDebugEnabled()) {
        runJoin("(warmup)", 2, 10, 10, JoinSession::lucas, false);
      }
    }

    @Test
    public void joinLhs010() {
      runJoin(testName.getMethodName(), doi(), 10, rhsWidth(), joinerFactory(), true);
    }

    @Test
    public void joinLhs020() {
      runJoin(testName.getMethodName(), doi(), 20, rhsWidth(), joinerFactory(), false);
    }

    @Test
    public void joinLhs030() {
      runJoin(testName.getMethodName(), doi(), 30, rhsWidth(), joinerFactory(), false);
    }

    @Test
    public void joinLhs040() {
      runJoin(testName.getMethodName(), doi(), 40, rhsWidth(), joinerFactory(), false);
    }

    @Test
    public void joinLhs050() {
      runJoin(testName.getMethodName(), doi(), 50, rhsWidth(), joinerFactory(), false);
    }

    @Test
    public void joinLhs060() {
      runJoin(testName.getMethodName(), doi(), 60, rhsWidth(), joinerFactory(), false);
    }

    @Test
    public void joinLhs070() {
      runJoin(testName.getMethodName(), doi(), 70, rhsWidth(), joinerFactory(), false);
    }

    @Test
    public void joinLhs080() {
      runJoin(testName.getMethodName(), doi(), 80, rhsWidth(), joinerFactory(), false);
    }

    @Test
    public void joinLhs090() {
      runJoin(testName.getMethodName(), doi(), 90, rhsWidth(), joinerFactory(), false);
    }

    @Test
    public void joinLhs100() {
      runJoin(testName.getMethodName(), doi(), 100, rhsWidth(), joinerFactory(), false);
    }

    abstract Function<Requirement, Joiner> joinerFactory();

    abstract int doi();

    abstract int rhsWidth();
  }

}

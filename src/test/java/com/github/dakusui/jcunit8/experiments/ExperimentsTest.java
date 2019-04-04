package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.testutils.testsuitequality.FactorSpaceSpec;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.function.Function;

import static com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils.generateWithIpoGplus;

@RunWith(Enclosed.class)
public class ExperimentsTest {
  public static class JoinExperimentTest extends JoinExperimentBase {
    @Override
    protected int strength() {
      return 2;
    }

    @Override
    protected FactorSpaceSpec lhsFactorSpaceSpec() {
      return new FactorSpaceSpec("L").addFactor(2, 5);
    }

    @Test
    public void whenJoinWith2_10$thenLetsSee() {
      final com.github.dakusui.jcunit8.factorspace.FactorSpace r = new FactorSpaceSpec("R").addFactor(2, 10).build();
      exerciseJoin(
          r,
          10, generateWithIpoGplus(
              r, strength()
          ));
    }

    @Test
    public void whenJoinWith2_20$thenLetsSee() {
      final com.github.dakusui.jcunit8.factorspace.FactorSpace r = new FactorSpaceSpec("R").addFactor(2, 20).build();
      exerciseJoin(
          r,
          10, generateWithIpoGplus(
              r, strength()
          ));
    }

    @Test
    public void whenJoinWith2_30$thenLetsSee() {
      final com.github.dakusui.jcunit8.factorspace.FactorSpace r = new FactorSpaceSpec("R").addFactor(2, 30).build();
      exerciseJoin(
          r,
          10, generateWithIpoGplus(
              r, strength()
          ));
    }

    @Test
    public void whenJoinWith2_40$thenLetsSee() {
      final com.github.dakusui.jcunit8.factorspace.FactorSpace r = new FactorSpaceSpec("R").addFactor(2, 40).build();
      exerciseJoin(
          r,
          10, generateWithIpoGplus(
              r, strength()
          ));
    }
  }

  public static class JoinExperimentWithStrength3Test extends JoinExperimentBase {
    @Override
    protected int strength() {
      return 3;
    }

    @Override
    protected FactorSpaceSpec lhsFactorSpaceSpec() {
      return new FactorSpaceSpec("L").addFactor(2, 20);
    }

    @Test
    public void whenJoinWith2_10$thenLetsSee() {
      final com.github.dakusui.jcunit8.factorspace.FactorSpace r = new FactorSpaceSpec("R").addFactor(2, 20).build();
      exerciseJoin(
          r,
          10, generateWithIpoGplus(
              r, strength()
          ));
    }
  }

  public static class JoinExperimentWithStrength3UsingWeakenProductMethodTest extends JoinExperimentBase {
    @Override
    protected int strength() {
      return 4;
    }

    @Override
    protected FactorSpaceSpec lhsFactorSpaceSpec() {
      return new FactorSpaceSpec("L").addFactor(2, 20);
    }

    @Test
    public void whenJoinWith2_10$thenLetsSee() {
      final com.github.dakusui.jcunit8.factorspace.FactorSpace r = new FactorSpaceSpec("R").addFactor(2, 20).build();
      exerciseJoin(r, 10, generateWithIpoGplus(r, strength()));
    }

    @Override
    protected Function<Requirement, Joiner> joinerFactory() {
      return Joiner.WeakenProduct::new;
    }
  }
}

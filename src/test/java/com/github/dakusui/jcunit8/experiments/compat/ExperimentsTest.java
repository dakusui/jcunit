package com.github.dakusui.jcunit8.experiments.compat;

import com.github.dakusui.jcunit8.testutils.testsuitequality.CompatFactorSpaceSpec;
import com.github.dakusui.jcunit8.extras.normalizer.FactorSpaceSpec;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.function.Function;

import static com.github.dakusui.jcunit8.experiments.join.JoinExperimentUtils.loadPregeneratedOrGenerateAndSaveCoveringArrayFor;
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
      return factorSpeceSpec("L", 5);
    }

    @Test
    public void whenJoinWith2_10$thenLetsSee() {
      FactorSpaceSpec rSpec = factorSpeceSpec("R", 10);
      final FactorSpace r = rSpec.build();
      exerciseJoin(10, rSpec, generateWithIpoGplus(r, strength()));
    }

    @Test
    public void whenJoinWith2_20$thenLetsSee() {
      FactorSpaceSpec rSpec = factorSpeceSpec("R", 20);
      final FactorSpace r = rSpec.build();
      exerciseJoin(10, rSpec, generateWithIpoGplus(r, strength()));
    }

    @Test
    public void whenJoinWith2_30$thenLetsSee() {
      FactorSpaceSpec rSpec = factorSpeceSpec("R", 30);
      final FactorSpace r = rSpec.build();
      exerciseJoin(10, rSpec, generateWithIpoGplus(r, strength()));
    }

    @Test
    public void whenJoinWith2_40$thenLetsSee() {
      FactorSpaceSpec rSpec = factorSpeceSpec("R", 40);
      final FactorSpace r = rSpec.build();
      exerciseJoin(10, rSpec, generateWithIpoGplus(r, strength()));
    }
  }

  /**
   * This experiment consumes too much time for daily unit testing and it is
   * ignored by default.
   */
  @Ignore
  public static class JoinExperimentWithStrength3Test extends JoinExperimentBase {
    @Override
    protected int strength() {
      return 3;
    }

    @Override
    protected FactorSpaceSpec lhsFactorSpaceSpec() {
      return new CompatFactorSpaceSpec("L").addFactors(2, 20);
    }

    @Test
    public void whenJoinWith2_10$thenLetsSee() {
      FactorSpaceSpec rSpec = new CompatFactorSpaceSpec("R").addFactors(2, 20);
      final FactorSpace r = rSpec.build();
      exerciseJoin(
          10,
          rSpec,
          loadPregeneratedOrGenerateAndSaveCoveringArrayFor(
              rSpec,
              strength(),
              CoveringArrayGenerationUtils::generateWithIpoGplus));
    }
  }

  public static class JoinExperimentWithStrength3UsingWeakenProductMethodTest extends JoinExperimentBase {
    @Override
    protected int strength() {
      return 3;
    }

    @Override
    protected FactorSpaceSpec lhsFactorSpaceSpec() {
      return new CompatFactorSpaceSpec("L").addFactors(2, 20);
    }

    @Test
    public void whenJoinWith2_10$thenLetsSee() {
      FactorSpaceSpec rSpec = new CompatFactorSpaceSpec("R").addFactors(2, 20);
      exerciseJoin(10,
          rSpec,
          loadPregeneratedOrGenerateAndSaveCoveringArrayFor(
              rSpec,
              strength(),
              CoveringArrayGenerationUtils::generateWithIpoGplus));
    }

    @Override
    protected Function<Requirement, Joiner> joinerFactory() {
      return Joiner.WeakenProduct::new;
    }
  }
}

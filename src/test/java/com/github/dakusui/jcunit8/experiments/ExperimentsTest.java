package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.jcunit8.testutils.testsuitequality.FactorSpaceSpec;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

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

    @Override
    @Test
    @Ignore
    public void whenJoinWith2_50$thenLetsSee() {
    }

    @Override
    @Test
    @Ignore
    public void whenJoinWith2_60$thenLetsSee() {
    }

    @Override
    @Test
    @Ignore
    public void whenJoinWith2_70$thenLetsSee() {
    }

    @Override
    @Test
    @Ignore
    public void whenJoinWith2_80$thenLetsSee() {
    }


    @Override
    @Test
    @Ignore
    public void whenJoinWith2_90$thenLetsSee() {
    }

  }

  public static class JoinExperimentWithStrength3Test extends JoinExperimentBase {
    @Override
    protected int strength() {
      return 3;
    }

    @Override
    protected FactorSpaceSpec lhsFactorSpaceSpec() {
      return new FactorSpaceSpec("L").addFactor(2, 5);
    }

    @Override
    @Test
    @Ignore
    public void whenJoinWith2_20$thenLetsSee() {
    }

    @Override
    @Test
    @Ignore
    public void whenJoinWith2_30$thenLetsSee() {
    }

    @Override
    @Test
    @Ignore
    public void whenJoinWith2_40$thenLetsSee() {
    }


    @Override
    @Test
    @Ignore
    public void whenJoinWith2_50$thenLetsSee() {
    }

    @Override
    @Test
    @Ignore
    public void whenJoinWith2_60$thenLetsSee() {
    }

    @Override
    @Test
    @Ignore
    public void whenJoinWith2_70$thenLetsSee() {
    }

    @Override
    @Test
    @Ignore
    public void whenJoinWith2_80$thenLetsSee() {
    }


    @Override
    @Test
    @Ignore
    public void whenJoinWith2_90$thenLetsSee() {
    }

  }
}

package com.github.dakusui.jcunit8.experiments.compat;

import com.github.dakusui.jcunit8.experiments.peerj.CompatFactorSpaceSpecForExperiments;
import com.github.dakusui.jcunit8.experiments.peerj.FactorSpaceSpecForExperiments;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class JoinExperiments {
  public static class Lhs10 extends JoinExperimentRhs2_n {
    @Override
    protected int strength() {
      return 2;
    }

    @Override
    protected FactorSpaceSpecForExperiments lhsFactorSpaceSpec() {
      return new CompatFactorSpaceSpecForExperiments("L").addFactors(2, 10);
    }
  }

  public static class Lhs20 extends JoinExperimentRhs2_n {
    @Override
    protected int strength() {
      return 2;
    }

    @Override
    protected FactorSpaceSpecForExperiments lhsFactorSpaceSpec() {
      return new CompatFactorSpaceSpecForExperiments("L").addFactors(2, 20);
    }
  }

  public static class Lhs30 extends JoinExperimentRhs2_n {
    @Override
    protected int strength() {
      return 2;
    }

    @Override
    protected FactorSpaceSpecForExperiments lhsFactorSpaceSpec() {
      return new CompatFactorSpaceSpecForExperiments("L").addFactors(2, 30);
    }
  }

  public static class Lhs40 extends JoinExperimentRhs2_n {
    @Override
    protected int strength() {
      return 2;
    }

    @Override
    protected FactorSpaceSpecForExperiments lhsFactorSpaceSpec() {
      return new CompatFactorSpaceSpecForExperiments("L").addFactors(2, 40);
    }
  }

  public static class Lhs50 extends JoinExperimentRhs2_n {
    @Override
    protected int strength() {
      return 2;
    }

    @Override
    protected FactorSpaceSpecForExperiments lhsFactorSpaceSpec() {
      return new CompatFactorSpaceSpecForExperiments("L").addFactors(2, 50);
    }
  }

  public static class Lhs60 extends JoinExperimentRhs2_n {
    @Override
    protected int strength() {
      return 2;
    }

    @Override
    protected FactorSpaceSpecForExperiments lhsFactorSpaceSpec() {
      return new CompatFactorSpaceSpecForExperiments("L").addFactors(2, 60);
    }
  }

  public static class Lhs70 extends JoinExperimentRhs2_n {
    @Override
    protected int strength() {
      return 2;
    }

    @Override
    protected FactorSpaceSpecForExperiments lhsFactorSpaceSpec() {
      return new CompatFactorSpaceSpecForExperiments("L").addFactors(2, 70);
    }
  }

  public static class Lhs80 extends JoinExperimentRhs2_n {
    @Override
    protected int strength() {
      return 2;
    }

    @Override
    protected FactorSpaceSpecForExperiments lhsFactorSpaceSpec() {
      return new CompatFactorSpaceSpecForExperiments("L").addFactors(2, 80);
    }
  }

  public static class Lhs90 extends JoinExperimentRhs2_n {
    @Override
    protected int strength() {
      return 2;
    }

    @Override
    protected FactorSpaceSpecForExperiments lhsFactorSpaceSpec() {
      return new CompatFactorSpaceSpecForExperiments("L").addFactors(2, 90);
    }
  }
}

package com.github.dakusui.jcunit8.experiments;

import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Enclosed.class)
public class JoinExperiments {
  public static class Lhs10 extends JoinExperimentBase {
    @Override
    protected int strength() {
      return 2;
    }

    @Override
    protected FactorSpaceSpec lhsFactorSpaceSpec() {
      return new FactorSpaceSpec("L").addFactor(2, 10);
    }
  }

  public static class Lhs20 extends JoinExperimentBase {
    @Override
    protected int strength() {
      return 2;
    }

    @Override
    protected FactorSpaceSpec lhsFactorSpaceSpec() {
      return new FactorSpaceSpec("L").addFactor(2, 20);
    }
  }

  public static class Lhs30 extends JoinExperimentBase {
    @Override
    protected int strength() {
      return 2;
    }

    @Override
    protected FactorSpaceSpec lhsFactorSpaceSpec() {
      return new FactorSpaceSpec("L").addFactor(2, 30);
    }
  }

  public static class Lhs40 extends JoinExperimentBase {
    @Override
    protected int strength() {
      return 2;
    }

    @Override
    protected FactorSpaceSpec lhsFactorSpaceSpec() {
      return new FactorSpaceSpec("L").addFactor(2, 40);
    }
  }

  public static class Lhs50 extends JoinExperimentBase {
    @Override
    protected int strength() {
      return 2;
    }

    @Override
    protected FactorSpaceSpec lhsFactorSpaceSpec() {
      return new FactorSpaceSpec("L").addFactor(2, 50);
    }
  }

  public static class Lhs60 extends JoinExperimentBase {
    @Override
    protected int strength() {
      return 2;
    }

    @Override
    protected FactorSpaceSpec lhsFactorSpaceSpec() {
      return new FactorSpaceSpec("L").addFactor(2, 60);
    }
  }

  public static class Lhs70 extends JoinExperimentBase {
    @Override
    protected int strength() {
      return 2;
    }

    @Override
    protected FactorSpaceSpec lhsFactorSpaceSpec() {
      return new FactorSpaceSpec("L").addFactor(2, 70);
    }
  }

  public static class Lhs80 extends JoinExperimentBase {
    @Override
    protected int strength() {
      return 2;
    }

    @Override
    protected FactorSpaceSpec lhsFactorSpaceSpec() {
      return new FactorSpaceSpec("L").addFactor(2, 80);
    }
  }

  public static class Lhs90 extends JoinExperimentBase {
    @Override
    protected int strength() {
      return 2;
    }

    @Override
    protected FactorSpaceSpec lhsFactorSpaceSpec() {
      return new FactorSpaceSpec("L").addFactor(2, 90);
    }
  }

  @RunWith(Suite.class)
  @Suite.SuiteClasses(
      value = {
          Lhs70.class,
          Lhs60.class
      }
  )
  public static class ExperimentSuite {
  }
}

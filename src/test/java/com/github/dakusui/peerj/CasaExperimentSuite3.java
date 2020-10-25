package com.github.dakusui.peerj;

import org.junit.runners.Parameterized.Parameters;

import java.util.List;

public class CasaExperimentSuite3 {
  public static class CasaExperimentParameterized9 extends CasaExperimentParameterized {
    public CasaExperimentParameterized9(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return parameters(40, 45);
    }
  }

  public static class CasaExperimentParameterized10 extends CasaExperimentParameterized {
    public CasaExperimentParameterized10(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return parameters(45, 50);
    }
  }

  public static class CasaExperimentParameterized11 extends CasaExperimentParameterized {
    public CasaExperimentParameterized11(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return parameters(50, 55);
    }
  }

  public static class CasaExperimentParameterized12 extends CasaExperimentParameterized {
    public CasaExperimentParameterized12(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return parameters(55, 60);
    }
  }
}

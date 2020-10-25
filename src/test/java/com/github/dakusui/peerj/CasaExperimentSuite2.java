package com.github.dakusui.peerj;

import org.junit.runners.Parameterized;

import java.util.List;

public class CasaExperimentSuite2 {
  public static class CasaExperimentParameterized5 extends CasaExperimentParameterized {
    public CasaExperimentParameterized5(Spec spec) {
      super(spec);
    }

    @Parameterized.Parameters
    public static List<Spec> parameters() {
      return parameters(20, 25);
    }
  }

  public static class CasaExperimentParameterized6 extends CasaExperimentParameterized {
    public CasaExperimentParameterized6(Spec spec) {
      super(spec);
    }

    @Parameterized.Parameters
    public static List<Spec> parameters() {
      return parameters(25, 30);
    }
  }

  public static class CasaExperimentParameterized7 extends CasaExperimentParameterized {
    public CasaExperimentParameterized7(Spec spec) {
      super(spec);
    }

    @Parameterized.Parameters
    public static List<Spec> parameters() {
      return parameters(30, 35);
    }
  }

  public static class CasaExperimentParameterized8 extends CasaExperimentParameterized {
    public CasaExperimentParameterized8(Spec spec) {
      super(spec);
    }

    @Parameterized.Parameters
    public static List<Spec> parameters() {
      return parameters(35, 40);
    }
  }
}

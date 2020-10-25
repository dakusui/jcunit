package com.github.dakusui.peerj;

import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

@RunWith(Enclosed.class)
public class CasaExperimentSuite1 {
  public static class CasaExperimentParameterized1 extends CasaExperimentParameterized {
    public CasaExperimentParameterized1(Spec spec) {
      super(spec);
    }

    @Parameterized.Parameters
    public static List<Spec> parameters() {
      return parameters(0, 5);
    }
  }

  public static class CasaExperimentParameterized2 extends CasaExperimentParameterized {
    public CasaExperimentParameterized2(Spec spec) {
      super(spec);
    }

    @Parameterized.Parameters
    public static List<Spec> parameters() {
      return parameters(5, 10);
    }
  }

  public static class CasaExperimentParameterized3 extends CasaExperimentParameterized {
    public CasaExperimentParameterized3(Spec spec) {
      super(spec);
    }

    @Parameterized.Parameters
    public static List<Spec> parameters() {
      return parameters(10, 15);
    }
  }

  public static class CasaExperimentParameterized4 extends CasaExperimentParameterized {
    public CasaExperimentParameterized4(Spec spec) {
      super(spec);
    }

    @Parameterized.Parameters
    public static List<Spec> parameters() {
      return parameters(15, 20);
    }
  }
}

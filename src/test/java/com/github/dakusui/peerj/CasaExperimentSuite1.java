package com.github.dakusui.peerj;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MINUTES;

@RunWith(Enclosed.class)
public class CasaExperimentSuite1 {
  public static class Lower extends CasaExperimentParameterized {
    public Lower(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return parameters(each -> each.categoryName.equals("Real"), asList(2, 3));
    }
  }

  public static class Apache4 extends CasaExperimentParameterized {
    @Rule
    public Timeout globalTimeout = new Timeout(20, MINUTES);

    public Apache4(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return parameters(each -> (each.categoryName.equals("Real") && each.modelName.endsWith("apache")), singletonList(4));
    }
  }

  public static class Bugzilla4 extends CasaExperimentParameterized {
    @Rule
    public Timeout globalTimeout = new Timeout(20, MINUTES);

    public Bugzilla4(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return parameters(each -> (each.categoryName.equals("Real") && each.modelName.endsWith("bugzilla")), singletonList(4));
    }
  }

  public static class Gcc3 extends CasaExperimentParameterized {
    public Gcc3(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return parameters(each -> (each.categoryName.equals("Real") && each.modelName.endsWith("gcc")), singletonList(3));
    }

    @Ignore
    @Test
    public void joinWithStandardPartitioner() {
      super.joinWithStandardPartitioner();
    }
  }

  public static class Gcc4 extends CasaExperimentParameterized {
    @Rule
    public Timeout globalTimeout = new Timeout(40, MINUTES);

    public Gcc4(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return parameters(each -> (each.categoryName.equals("Real") && each.modelName.endsWith("gcc")), singletonList(4));
    }

    @Ignore
    @Test
    public void joinWithStandardPartitioner() {
      super.joinWithStandardPartitioner();
    }
  }

  public static class Gcc5 extends CasaExperimentParameterized {
    @Rule
    public Timeout globalTimeout = new Timeout(20, MINUTES);

    public Gcc5(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return parameters(each -> (each.categoryName.equals("Real") && each.modelName.endsWith("gcc")), singletonList(5));
    }
  }

  public static class Gcc6 extends CasaExperimentParameterized {
    @Rule
    public Timeout globalTimeout = new Timeout(30, MINUTES);

    public Gcc6(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return parameters(each -> (each.categoryName.equals("Real") && each.modelName.endsWith("gcc")), singletonList(6));
    }
  }

  public static class Spins4 extends CasaExperimentParameterized {
    @Rule
    public Timeout globalTimeout = new Timeout(20, MINUTES);

    public Spins4(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return parameters(each -> (each.categoryName.equals("Real") && each.modelName.endsWith("spins")), singletonList(4));
    }
  }

  public static class Spins5 extends CasaExperimentParameterized {
    @Rule
    public Timeout globalTimeout = new Timeout(20, MINUTES);

    public Spins5(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return parameters(each -> (each.categoryName.equals("Real") && each.modelName.endsWith("spins")), singletonList(4));
    }
  }

  public static class Spinv4 extends CasaExperimentParameterized {
    @Rule
    public Timeout globalTimeout = new Timeout(20, MINUTES);

    public Spinv4(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return parameters(each -> (each.categoryName.equals("Real") && each.modelName.endsWith("spinv")), singletonList(4));
    }
  }

  public static class Tcas4 extends CasaExperimentParameterized {
    @Rule
    public Timeout globalTimeout = new Timeout(20, MINUTES);

    public Tcas4(Spec spec) {
      super(spec);
    }

    @Parameters
    public static List<Spec> parameters() {
      return parameters(each -> (each.categoryName.equals("Real") && each.modelName.endsWith("tcas")), singletonList(4));
    }
  }
}

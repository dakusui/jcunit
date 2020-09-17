package com.github.dakusui.jcunit8.experiments.join.basic;

import com.github.dakusui.jcunit8.experiments.join.JoinExperiment;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import org.junit.Ignore;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;

import java.util.LinkedList;
import java.util.List;

@RunWith(Enclosed.class)
public class WeakenAndProduct {
  public static class RunthroughStrength2and3 extends JoinExperimentBase {
    public RunthroughStrength2and3(JoinExperiment experiment) {
      super(experiment);
    }

    @Parameters
    public static List<JoinExperiment> experiments() {
      List<JoinExperiment> work = new LinkedList<>();
      for (int t = 2; t <= 3; t++)
        for (int i = 100; i < 500; i += 100)
          for (int j = 100; j < 500; j += 100)
            work.add(createExperiment(i, j, t, Joiner.WeakenProduct::new));
      return work;
    }
  }

  public static class RunthroughStrength2and3b extends JoinExperimentBase {
    public RunthroughStrength2and3b(JoinExperiment experiment) {
      super(experiment);
    }

    @Parameters
    public static List<JoinExperiment> experiments() {
      List<JoinExperiment> work = new LinkedList<>();
      for (int t = 2; t <= 3; t++)
        for (int i = 100; i < 1000; i += 100)
          work.add(createExperiment(i, i, t, Joiner.WeakenProduct::new));
      return work;
    }
  }

  public static class Benchmark extends JoinExperimentBase {
    public Benchmark(JoinExperiment experiment) {
      super(experiment);
    }

    @Parameters
    public static List<JoinExperiment> experiments() {
      List<JoinExperiment> work = new LinkedList<>();
      int t = 5;
      int degree = 20;
      work.add(createExperiment(degree, degree, t, Joiner.WeakenProduct::new));
      return work;
    }
  }

  public static class HigherStrength extends JoinExperimentBase {
    public HigherStrength(JoinExperiment experiment) {
      super(experiment);
    }

    @Parameters
    public static List<JoinExperiment> experiments() {
      List<JoinExperiment> work = new LinkedList<>();
      for (int t = 2; t < 7; t++) {
        int i = 20;
        int j = 20;
        work.add(createExperiment(i, j, t, Joiner.WeakenProduct::new));
      }
      return work;
    }
  }

  @Ignore
  public static class HigherStrengthWithStandardJoiner extends JoinExperimentBase {
    public HigherStrengthWithStandardJoiner(JoinExperiment experiment) {
      super(experiment);
    }

    @Parameters
    public static List<JoinExperiment> experiments() {
      List<JoinExperiment> work = new LinkedList<>();
      for (int t = 2; t <= 4; t++) {
        int i = 10;
        int j = 10;
        work.add(createExperiment(i, j, t, Joiner.Standard::new));
      }
      return work;
    }
  }
}

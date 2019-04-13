package com.github.dakusui.jcunit8.experiments.join.basic;

import com.github.dakusui.jcunit8.experiments.join.JoinExperiment;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import org.junit.Test;
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
        for (int i = 10; i < 100; i += 10)
          for (int j = 10; j < 100; j += 10)
            work.add(createExperiment(i, j, t, Joiner.WeakenProduct::new));
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
      for (int t = 2; t <= 7; t++) {
        int i = 10;
        int j = 10;
        work.add(createExperiment(i, j, t, Joiner.WeakenProduct::new));
      }
      return work;
    }
  }

  public static class HigherStrengthWithStandardJoiner extends JoinExperimentBase {
    public HigherStrengthWithStandardJoiner(JoinExperiment experiment) {
      super(experiment);
    }

    @Parameters
    public static List<JoinExperiment> experiments() {
      List<JoinExperiment> work = new LinkedList<>();
      for (int t = 2; t <= 5; t++) {
        int i = 10;
        int j = 10;
        work.add(createExperiment(i, j, t, Joiner.Standard::new));
      }
      return work;
    }
  }

  public static class Smallest extends JoinExperimentBase {

    public Smallest(JoinExperiment experiment) {
      super(experiment);
    }

    @Parameters
    public static List<JoinExperiment> experiments() {
      List<JoinExperiment> work = new LinkedList<>();
      work.add(createExperiment(3, 3, 2, Joiner.Standard::new));
      return work;
    }

    @Test
    public void joinAndPrint() {
      super.joinAndPrint();
    }
  }
}

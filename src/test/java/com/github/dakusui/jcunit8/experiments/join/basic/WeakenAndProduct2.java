package com.github.dakusui.jcunit8.experiments.join.basic;

import com.github.dakusui.jcunit8.experiments.join.JoinExperiment;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import org.junit.runners.Parameterized.Parameters;

import java.util.LinkedList;
import java.util.List;

public class WeakenAndProduct2 extends JoinExperimentBase {
  public WeakenAndProduct2(JoinExperiment experiment) {
    super(experiment);
  }

  @Parameters
  public static List<JoinExperiment> experiments() {
    List<JoinExperiment> work = new LinkedList<>();
    int t = 3;
    for (int i = 10; i < 100; i += 10)
      for (int j = 10; j < 100; j += 10)
        work.add(createExperiment(i, j, t, Joiner.WeakenProduct2::new));
    return work;
  }
}

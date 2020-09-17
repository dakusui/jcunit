package com.github.dakusui.jcunit8.experiments.join.basic;

import org.junit.runners.Parameterized;

import java.util.LinkedList;
import java.util.List;

public class Example extends JoinExperimentBase {
  public Example(Experiment experiment) {
    super(experiment);
  }

  @Parameterized.Parameters
  public static List<Experiment> experiments() {
    List<Experiment> work = new LinkedList<>();
    for (int t = 2; t <= 3; t++)
      for (int i = 100; i < 10000; i += 100)
        for (GenerationMode generationMode : new GenerationMode[] { GenerationMode.WITH_ACTS_FULL, GenerationMode.WITH_JOIN })
          work.add(createExperiment(t, i, 4, generationMode));
    return work;
  }
}

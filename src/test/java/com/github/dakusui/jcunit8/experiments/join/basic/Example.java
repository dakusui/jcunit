package com.github.dakusui.jcunit8.experiments.join.basic;

import com.github.dakusui.jcunit8.experiments.generation.ConstraintSet;
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
    for (ConstraintSet constraintSet : new ConstraintSet[] { ConstraintSet.BASIC, ConstraintSet.NONE })
      for (GenerationMode generationMode : new GenerationMode[] { GenerationMode.WITH_ACTS_FULL, GenerationMode.WITH_JOIN })
        for (int t = 2; t <= 3; t++)
          for (int i = 20; i < 60; i += 20)
            work.add(createExperiment(t, i, 4, generationMode, constraintSet));
    return work;
  }
}

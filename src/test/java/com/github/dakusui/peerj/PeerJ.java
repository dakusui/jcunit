package com.github.dakusui.peerj;

import com.github.dakusui.jcunit8.experiments.generation.ConstraintSet;
import com.github.dakusui.jcunit8.experiments.join.basic.JoinExperimentBase;
import org.junit.runners.Parameterized.Parameters;

import java.util.LinkedList;
import java.util.List;

public class PeerJ extends JoinExperimentBase {
  public PeerJ(Experiment experiment) {
    super(experiment);
  }

  @Parameters
  public static List<Experiment> experiments() {
    List<Experiment> work = new LinkedList<>();
    for (ConstraintSet constraintSet : new ConstraintSet[] { ConstraintSet.BASIC_PLUS, ConstraintSet.BASIC, ConstraintSet.NONE })
      for (PeerJUtils.GenerationMode generationMode : new PeerJUtils.GenerationMode[] { PeerJUtils.GenerationMode.WITH_ACTS_FULL, PeerJUtils.GenerationMode.WITH_JOIN })
        for (int t = 2; t <= 3; t++)
          for (int i = 20; i < 60; i += 20)
            work.add(PeerJUtils.createExperiment(t, i, 4, generationMode, constraintSet));
    return work;
  }
}

package com.github.dakusui.peerj.runners;

import com.github.dakusui.peerj.acts.ActsExperiment;
import com.github.dakusui.peerj.model.ConstraintSet;
import com.github.dakusui.peerj.model.Experiment;
import com.github.dakusui.peerj.utils.PeerJUtils;
import org.junit.runners.Parameterized.Parameters;

import java.util.LinkedList;
import java.util.List;

public class PeerJ extends ExperimentRunner {
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
            work.add(PeerJUtils.createExperiment(t, i, 4, generationMode, constraintSet, new ActsExperiment.ActsOpts("ipog", "solver")));
    return work;
  }
}

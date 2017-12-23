package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.function.Function;

@RunWith(Enclosed.class)
public class IncrementalJoinExperiment {
  public static class WithStandardJoiner extends IncrementalJoinExperimentBase {
    Function<Requirement, Joiner> joinerFactory() {
      return JoinSession::standard;
    }
  }
  public static class WithIncrementalJoiner extends IncrementalJoinExperimentBase {
    Function<Requirement, Joiner> joinerFactory() {
      return JoinSession::incremental;
    }
  }

}


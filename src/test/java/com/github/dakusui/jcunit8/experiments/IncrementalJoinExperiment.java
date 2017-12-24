package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.function.Function;

@RunWith(Enclosed.class)
public class IncrementalJoinExperiment {
  public static class WithStandardJoiner_Doi2 extends IncrementalJoinExperimentBase {
    Function<Requirement, Joiner> joinerFactory() {
      return JoinSession::standard;
    }

    @Override
    int doi() {
      return 2;
    }
  }
  public static class WithIncrementalJoiner_Doi2 extends IncrementalJoinExperimentBase {
    Function<Requirement, Joiner> joinerFactory() {
      return JoinSession::incremental;
    }

    @Override
    int doi() {
      return 2;
    }
  }

  public static class WithIncrementalJoiner_Doi3 extends IncrementalJoinExperimentBase {
    Function<Requirement, Joiner> joinerFactory() {
      return JoinSession::incremental;
    }

    @Override
    int doi() {
      return 3;
    }
  }
}


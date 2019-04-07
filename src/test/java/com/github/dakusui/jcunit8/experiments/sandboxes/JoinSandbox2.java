package com.github.dakusui.jcunit8.experiments.sandboxes;

import com.github.dakusui.jcunit8.experiments.compat.CompatJoinExperimentUtils;
import com.github.dakusui.jcunit8.experiments.compat.CompatJoinReport;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class JoinSandbox2 {
  public static class Base {
    @BeforeClass
    public static void printHeader() {
      System.out.println(CompatJoinReport.header());
    }
  }

  public static class Even extends Base {
    @Test
    public void givenLhs10Rhs10$whenExercise$thenReported() {
      CompatJoinExperimentUtils.exerciseStandardExperiment10Times(10, 10);
    }

    @Test
    public void givenLhs20Rhs20$whenExercise$thenReported() {
      CompatJoinExperimentUtils.exerciseStandardExperiment10Times(20, 10);
    }

    @Test
    public void givenLhs30Rhs30$whenExercise$thenReported() {
      CompatJoinExperimentUtils.exerciseStandardExperiment10Times(30, 30);
    }

    @Test
    public void givenLhs40Rhs40$whenExercise$thenReported() {
      CompatJoinExperimentUtils.exerciseStandardExperiment10Times(40, 40);
    }

    @Test
    public void givenLhs50Rhs50$whenExercise$thenReported() {
      CompatJoinExperimentUtils.exerciseStandardExperiment10Times(50, 50);
    }

    @Test
    public void givenLhs55Rhs55$whenExercise$thenReported() {
      CompatJoinExperimentUtils.exerciseStandardExperiment10Times(55, 55);
    }

    @Test
    public void givenLhs60Rhs60$whenExercise$thenReported() {
      CompatJoinExperimentUtils.exerciseStandardExperiment10Times(60, 60);
    }
  }

  public static class Uneven100 extends Base {
    @Test
    public void givenLhs60Rhs40$whenExercise$thenReported() {
      CompatJoinExperimentUtils.exerciseStandardExperiment10Times(60, 40);
    }

    @Test
    public void givenLhs70Rhs30$whenExercise$thenReported() {
      CompatJoinExperimentUtils.exerciseStandardExperiment10Times(70, 30);
    }

    @Test
    public void givenLhs80Rhs20$whenExercise$thenReported() {
      CompatJoinExperimentUtils.exerciseStandardExperiment10Times(80, 20);
    }

    @Test
    public void givenLhs90Rhs10$whenExercise$thenReported() {
      CompatJoinExperimentUtils.exerciseStandardExperiment10Times(90, 10);
    }
  }

  public static class Uneven110 extends Base {
    @Test
    public void givenLhs60Rhs50$whenExercise$thenReported() {
      CompatJoinExperimentUtils.exerciseStandardExperiment10Times(60, 50);
    }

    @Test
    public void givenLhs70Rhs40$whenExercise$thenReported() {
      CompatJoinExperimentUtils.exerciseStandardExperiment10Times(70, 40);
    }

    @Test
    public void givenLhs80Rhs30$whenExercise$thenReported() {
      CompatJoinExperimentUtils.exerciseStandardExperiment10Times(80, 30);
    }

    @Test
    public void givenLhs90Rhs20$whenExercise$thenReported() {
      CompatJoinExperimentUtils.exerciseStandardExperiment10Times(90, 20);
    }

    @Test
    public void givenLhs100Rhs10$whenExercise$thenReported() {
      CompatJoinExperimentUtils.exerciseStandardExperiment10Times(100, 10);
    }
  }

  public static class Uneven120 extends Base {
    @Test
    public void givenLhs70Rhs50$whenExercise$thenReported() {
      CompatJoinExperimentUtils.exerciseStandardExperiment10Times(70, 50);
    }

    @Test
    public void givenLhs80Rhs40$whenExercise$thenReported() {
      CompatJoinExperimentUtils.exerciseStandardExperiment10Times(80, 40);
    }

    @Test
    public void givenLhs90Rhs30$whenExercise$thenReported() {
      CompatJoinExperimentUtils.exerciseStandardExperiment10Times(90, 30);
    }

    @Test
    public void givenLhs100Rhs20$whenExercise$thenReported() {
      CompatJoinExperimentUtils.exerciseStandardExperiment10Times(100, 20);
    }

    @Test
    public void givenLhs110Rhs10$whenExercise$thenReported() {
      CompatJoinExperimentUtils.exerciseStandardExperiment10Times(110, 10);
    }
  }

}
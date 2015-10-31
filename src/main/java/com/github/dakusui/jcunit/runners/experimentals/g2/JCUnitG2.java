package com.github.dakusui.jcunit.runners.experimentals.g2;

import com.github.dakusui.jcunit.runners.standard.JCUnit;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;
import org.junit.runners.parameterized.BlockJUnit4ClassRunnerWithParameters;

import java.util.Arrays;
import java.util.List;

public class JCUnitG2 extends Suite {
  public JCUnitG2(Class<?> klass) throws Throwable {
    super(klass, createRunners());
  }

  private static List<Runner> createRunners() {
    return Arrays.asList(new Runner[]{
        new Runner() {
          @Override
          public Description getDescription() {
            return null;
          }

          @Override
          public void run(RunNotifier notifier) {

          }
        }
    });
  }
}

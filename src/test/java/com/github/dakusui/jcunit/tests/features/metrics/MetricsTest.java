package com.github.dakusui.jcunit.tests.features.metrics;

import com.github.dakusui.jcunit.coverage.CombinatorialMetrics;
import com.github.dakusui.jcunit.coverage.FSMMetrics;
import com.github.dakusui.jcunit.examples.fsm.digest.MessageDigestExample;
import com.github.dakusui.jcunit.fsm.FSMLevelsProvider;
import com.github.dakusui.jcunit.fsm.Story;
import com.github.dakusui.jcunit.plugins.caengines.Ipo2CoveringArrayEngine;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.*;
import com.github.dakusui.jcunit.testutils.Metatest;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.MessageDigest;


public class MetricsTest extends Metatest {
  public MetricsTest() {
    super(Example.class, 7, 0, 0);
  }

  @Test
  public void runAllTests() {
    this.runTests();
  }

  @RunWith(JCUnit.class)
  @GenerateCoveringArrayWith(
      engine = @Generator(Ipo2CoveringArrayEngine.class),
      reporters = {
          @Reporter(value = FSMMetrics.class, args = { @Value("messageDigestStory"), @Value("1")}),
          @Reporter(value = CombinatorialMetrics.class, args = { @Value("2") })
      }
  )
  public static class Example {
    @FactorField(
        levelsProvider = FSMLevelsProvider.class,
        args = { @Value("3") })
    public Story<MessageDigest, MessageDigestExample.Spec> messageDigestStory;

    @Test
    public void test() {

    }
  }
}

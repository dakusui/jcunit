package com.github.dakusui.jcunit.tests.features.metrics;

import com.github.dakusui.jcunit.coverage.FSMMetrics;
import com.github.dakusui.jcunit.examples.fsm.digest.MessageDigestExample;
import com.github.dakusui.jcunit.fsm.FSMLevelsProvider;
import com.github.dakusui.jcunit.fsm.Story;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.annotations.Reporter;
import com.github.dakusui.jcunit.runners.standard.annotations.Value;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.MessageDigest;


public class MetricsTest {
  @RunWith(JCUnit.class)
  public static class Example {
    @Reporter(FSMMetrics.class)
    @FactorField(
        levelsProvider = FSMLevelsProvider.class,
        providerParams = {@Value("3")})
    public Story<MessageDigest, MessageDigestExample.Spec> messageDigestStory;
  }


  @Test
  public void test() {

  }
}

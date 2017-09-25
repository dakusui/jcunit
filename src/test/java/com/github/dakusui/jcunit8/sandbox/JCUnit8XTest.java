package com.github.dakusui.jcunit8.sandbox;

import com.github.dakusui.jcunit8.examples.executionsequence.ExampleParameterSpace;
import com.github.dakusui.jcunit8.pipeline.stages.ConfigFactory;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8X;
import com.github.dakusui.jcunit8.testsuite.TestCase;
import org.junit.Test;
import org.junit.runners.model.TestClass;

public class JCUnit8XTest {
  @Test
  public void givenTestClass$whenBuildTestSuite$thenBuilt() {
    for (TestCase each : JCUnit8X.buildTestSuite(
        new TestClass(JCUnit8XExample.class),
        new TestClass(ExampleParameterSpace.class),
        new ConfigFactory.Default()
    )) {
      System.out.println(each);
    }
  }
}

package com.github.dakusui.jcunit8.sandbox;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.examples.executionsequence.ExampleParameterSpace;
import com.github.dakusui.jcunit8.pipeline.stages.ConfigFactory;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8X;
import com.github.dakusui.jcunit8.testsuite.TestCase;
import com.github.dakusui.jcunit8.testsuite.TestSuite;
import org.junit.Test;
import org.junit.runners.model.TestClass;

import java.util.function.Consumer;

public class JCUnit8XTest {
  @Test
  public void givenTestClass$whenBuildTestSuite$thenBuilt() {
    TestSuite testSuite = JCUnit8X.buildTestSuite(
        new TestClass(JCUnit8XExample.class),
        new TestClass(ExampleParameterSpace.class),
        new ConfigFactory.Default()
    );
    for (TestCase each : testSuite) {
      System.out.println(each);
    }
    for (Consumer<Tuple> beforeTestOracle : testSuite.beforeTestOracle()) {
      System.out.println(beforeTestOracle);
    }
  }
}

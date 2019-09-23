package com.github.dakusui.jcunit8.sandbox;

import com.github.dakusui.crest.utils.printable.Printable;
import com.github.dakusui.jcunit8.examples.executionsequence.ExampleParameterSpace;
import com.github.dakusui.jcunit8.pipeline.stages.ConfigFactory;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.testsuite.TestCase;
import com.github.dakusui.jcunit8.testsuite.TestSuite;
import org.junit.Test;
import org.junit.runners.model.TestClass;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.dakusui.crest.Crest.asListOf;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.crest.utils.printable.Functions.size;
import static com.github.dakusui.crest.utils.printable.Predicates.equalTo;

public class JCUnit8Test {
  @Test
  public void givenTestClass$whenBuildTestSuite$thenTestCasesBuilt() {
    assertThat(
        buildTestSuite(),
        asListOf(
            TestCase.class
        ).check(
            size(),
            equalTo(2)
        ).$()
    );
  }

  @Test
  public void givenTestClass$whenBuildTestSuite$thenConsumersRunBeforeTestOracleBuilt() {
    assertThat(
        buildTestSuite(),
        asListOf(
            Consumer.class,
            Printable.function(
                "toConsumerListWithBeforeTestOracleMethod",
                toList((TestSuite suite) -> suite.getScenario().preOracleProcedures())
            )
        ).check(
            size(),
            equalTo(1)
        ).$()
    );
  }

  @SuppressWarnings("unchecked")
  private static <I, O> Function<I, List<O>> toList(Function<I, List<?>> toListFunc) {
    return i -> new LinkedList<O>() {{
      addAll((Collection<? extends O>) toListFunc.apply(i));
    }};
  }

  private TestSuite buildTestSuite() {
    return buildTestSuite(JCUnit8Example.class, ExampleParameterSpace.class);
  }

  private static TestSuite buildTestSuite(Class javaTestClass, Class javaParameterSpaceClass) {
    return JCUnit8.buildTestSuite(
        new TestClass(javaTestClass),
        new TestClass(javaParameterSpaceClass),
        new ConfigFactory.Default()
    );
  }
}

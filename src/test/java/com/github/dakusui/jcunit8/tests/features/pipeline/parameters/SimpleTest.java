package com.github.dakusui.jcunit8.tests.features.pipeline.parameters;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.testsuite.TestCase;
import com.github.dakusui.jcunit8.tests.features.pipeline.testbase.PipelineTestBase;
import com.github.dakusui.jcunit8.tests.features.pipeline.testbase.TestSuiteUtils;
import org.junit.Test;

import static com.github.dakusui.jcunit8.tests.features.pipeline.testbase.UTUtils.sizeIs;
import static java.util.Arrays.asList;

public class SimpleTest extends PipelineTestBase {
  @Test
  public void test() {
    for (TestCase<Tuple> each : generateTestSuite(
        Parameter.Simple.Factory.of(asList("A1", "A2", "A3")).create("A"),
        Parameter.Simple.Factory.of(asList("B1", "B2", "B3")).create("B")
    )) {
      System.out.println(each.get());
    }
  }

  @Test
  public void whenBuildTestSuite3() {
    TestSuiteUtils.validateTestSuite(
        generateTestSuite(
            simpleParameterFactoryWithDefaultValues().create("simple1"),
            simpleParameterFactoryWithDefaultValues().create("simple2"),
            simpleParameterFactoryWithDefaultValues().create("simple3")
        ),
        matcher(
            sizeIs(name("==4", value -> value == 4))
        )
    );
  }


}

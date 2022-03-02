package com.github.dakusui.jcunitx.tests.features.pipeline.parameters;

import com.github.dakusui.jcunitx.metamodel.parameters.SimpleParameter;
import com.github.dakusui.jcunitx.testsuite.TestCase;
import com.github.dakusui.jcunitx.testutils.PipelineTestBase;
import com.github.dakusui.jcunitx.testutils.TestSuiteUtils;
import com.github.dakusui.jcunitx.testutils.UTUtils;
import org.junit.Test;

import static com.github.dakusui.jcunitx.testutils.UTUtils.sizeIs;
import static java.util.Arrays.asList;

public class SimpleParameterTest extends PipelineTestBase {
  @Test
  public void test() {
    for (TestCase each : generateTestSuite(
        SimpleParameter.Descriptor.of(asList("A1", "A2", "A3")).create("A"),
        SimpleParameter.Descriptor.of(asList("B1", "B2", "B3")).create("B")
    )) {
      System.out.println(each.getTestInput());
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
        UTUtils.matcher(
            sizeIs("==4", value -> value == 4)
        )
    );
  }
}

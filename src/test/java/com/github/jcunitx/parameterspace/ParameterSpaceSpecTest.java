package com.github.jcunitx.parameterspace;

import com.github.dakusui.jcunit8.testutils.TestBase;
import com.github.dakusui.jcunit8.ututiles.PipelineConfigBuilder;
import com.github.jcunit.factorspace.ParameterSpace;
import com.github.jcunit.model.ParameterSpaceSpec;
import com.github.jcunit.pipeline.Pipeline;
import com.github.jcunit.pipeline.PipelineSpec;
import com.github.jcunit.testsuite.TestSuite;
import org.junit.jupiter.api.Test;

import static com.github.valid8j.fluent.Expectations.assertStatement;
import static com.github.valid8j.fluent.Expectations.value;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
public class ParameterSpaceSpecTest extends TestBase {
  @Test
  public void testParameterSpaceSpec() {
    ParameterSpaceSpec spaceSpec = SpecTestUtils.createTestParameterSpaceSpec();

    assertStatement(value(spaceSpec).satisfies(spec -> spec.invoke("parameterNames")
                                                           .asListOf(String.class)
                                                           .satisfies(names -> names.size()
                                                                                    .toBe()
                                                                                    .equalTo(2))
                                                           .satisfies(names -> names.elementAt(0)
                                                                                    .satisfies()
                                                                                    .equalTo("p1"))
                                                           .satisfies(names -> names.elementAt(1)
                                                                                    .toBe()
                                                                                    .equalTo("p2")))
                                    .satisfies(spec -> spec.invoke("parameterSpecFor", "p1")
                                                           .invoke("valueResolvers")
                                                           .asList()
                                                           .size()
                                                           .toBe()
                                                           .equalTo(2))
                                    .satisfies(spec -> spec.invoke("parameterSpecFor", "p2")
                                                           .invoke("valueResolvers")
                                                           .asList()
                                                           .size()
                                                           .toBe()
                                                           .equalTo(2)));
  }

  @Test
  public void testToParameterSpace() {
    ParameterSpaceSpec testParameterSpaceSpec = SpecTestUtils.createTestParameterSpaceSpec();
    ParameterSpace parameterSpace = testParameterSpaceSpec.toParameterSpace(testParameterSpaceSpec.parameterNames());
    assertStatement(value(parameterSpace).invoke("getParameterNames")
                                         .asListOf(String.class)
                                         .elementAt(0).toBe().equalTo("p1"));
  }

  @Test
  public void testToTestSuite() {
    ParameterSpaceSpec testParameterSpaceSpec = SpecTestUtils.createTestParameterSpaceSpec();
    TestSuite testSuite = Pipeline.Standard.create(new PipelineSpec.Builder(new PipelineConfigBuilder().build())
                                                       .build())
                                           .execute(testParameterSpaceSpec.toParameterSpace(testParameterSpaceSpec.parameterNames()));
    testSuite.forEach(System.out::println);
  }
}

package com.github.jcunitx.parameterspace;

import com.github.jcunit.core.model.ParameterSpaceSpec;
import org.junit.jupiter.api.Test;

import static com.github.valid8j.fluent.Expectations.assertStatement;
import static com.github.valid8j.fluent.Expectations.value;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
public class ParameterSpaceSpecTest {
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
}

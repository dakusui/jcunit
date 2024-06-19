package com.github.jcunitx.parameterspace;

import com.github.jcunit.model.ParameterSpaceSpec;
import com.github.jcunit.model.ParameterSpec;
import com.github.jcunit.model.ValueResolver;
import com.github.jcunit.factorspace.Parameter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.valid8j.fluent.Expectations.assertStatement;
import static com.github.valid8j.fluent.Expectations.value;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
public class ParameterSpecTest {
  @Test
  public void testParameterSpace() {
    ParameterSpec<String> spec = SpecTestUtils.createTestParameterSpecP1();

    assertStatement(value(spec).satisfies(x -> x.invoke("valueResolvers")
                                                .asList()
                                                .size()
                                                .toBe()
                                                .equalTo(2))
                               .invoke("name")
                               .asString()
                               .toBe()
                               .equalTo("p1"));
  }

  @Test
  public void testParameterSpecToParameter() {
    ParameterSpec<String> testParameterSpecP1 = SpecTestUtils.createTestParameterSpecP1();
    List<ParameterSpec<?>> parameterSpecs = asList(testParameterSpecP1, SpecTestUtils.createTestParameterSpecP2());
    ParameterSpaceSpec parameterSpaceSpec = ParameterSpaceSpec.create(parameterSpecs, emptyList());
    Parameter<List<ValueResolver<String>>> parameter = testParameterSpecP1.toParameter(parameterSpaceSpec, false);

    assertStatement(
        value(parameter).satisfies(p -> p.invoke("getName").asString().satisfies().equalTo("p1"))
                        .satisfies(p -> p.invoke("getKnownValues").asList().size().satisfies().equalTo(2)));
  }
}

package com.github.jcunitx.parameterspace;

import com.github.jcunit.annotations.JCUnitParameter;
import com.github.jcunit.model.ParameterSpaceSpec;
import com.github.jcunit.model.ParameterSpec;
import com.github.jcunit.model.ValueResolver;
import com.github.jcunit.pipeline.PipelineConfig;
import com.github.jcunit.pipeline.Requirement;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
public enum SpecTestUtils {
  ;

  static ParameterSpaceSpec createTestParameterSpaceSpec() {
    return createParameterSpaceSpec(createTestParameterSpecP1(), createTestParameterSpecP2());
  }

  static ParameterSpaceSpec createParameterSpaceSpec(ParameterSpec<String> p1, ParameterSpec<String> p2) {
    return ParameterSpaceSpec.create(
        new PipelineConfig.Builder(new Requirement.Builder().build()).build(),
        asList(p1, p2),
        emptyList());
  }

  static ParameterSpec<String> createTestParameterSpecP1() {
    return ParameterSpec.create("p1", JCUnitParameter.Type.SIMPLE, new String[0], ValueResolver.from("p1v1").$(), ValueResolver.from("p1v2").$());
  }

  static ParameterSpec<String> createTestParameterSpecP2() {
    return ParameterSpec.create("p2", JCUnitParameter.Type.SIMPLE, new String[0], ValueResolver.from("p2v1").$(), ValueResolver.from("p2v2").$());
  }
}

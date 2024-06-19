package com.github.jcunitx.parameterspace;

import com.github.jcunit.annotations.JCUnitParameter;
import com.github.jcunit.core.Invokable;
import com.github.jcunit.model.ParameterSpec;
import com.github.jcunit.model.ValueResolver;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
public enum SpecTestUtils {
  ;

  @SuppressWarnings("unchecked")
  static ParameterSpec<String> createTestParameterSpecP1() {
    return ParameterSpec.create("p1",
                                JCUnitParameter.Type.SIMPLE,
                                new String[0],
                                ValueResolver.from("p1v1").$(),
                                ValueResolver.from("p1v2").$());
  }

  static ParameterSpec<String> createTestParameterSpecP1_referenceP2() {
    return ParameterSpec.create("p1",
                                JCUnitParameter.Type.SIMPLE,
                                new String[0],
                                ValueResolver.from("p1v1").$(),
                                ValueResolver.fromInvokable(Invokable.referenceTo("p2", 0)));
  }
  @SuppressWarnings("unchecked")
  static ParameterSpec<String> createTestParameterSpecP2() {
    return ParameterSpec.create("p2",
                                JCUnitParameter.Type.SIMPLE,
                                new String[0],
                                ValueResolver.from("p2v1").$(),
                                ValueResolver.from("p2v2").$());
  }

}

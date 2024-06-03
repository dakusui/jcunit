package com.github.dakusui.jcunit8.sandbox.example;

import com.github.jcunit.annotations.*;
import com.github.jcunit.core.model.ParameterSpaceSpec;
import com.github.jcunit.core.model.ParameterSpec;
import com.github.jcunit.core.model.ValueResolver;
import com.github.jcunit.runners.junit5.JCUnitTestExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
@Disabled
@UsingParameterSpace(TestExample2.class)
@ExtendWith(JCUnitTestExtension.class)
public class TestExample2 {
  @Named
  @JCUnitParameter
  public static List<ValueResolver<String>> param1() {
    return asList(ValueResolver.from("hello").$(),
                  ValueResolver.from("world").$(),
                  ValueResolver.with(t -> (String) t.get("param3")).$("param3"),
                  ValueResolver.from(TestExample2.class).classMethodNamed("param1Value1")
    );
  }

  @Named
  @JCUnitParameter
  public static List<ValueResolver<Map<String, List<String>>>> param2() {
    return asList(ValueResolver.from(Collections.singletonMap("K2", singletonList("V2"))).$(),
                  ValueResolver.from(Collections.singletonMap("K1", singletonList("V1"))).$());
  }

  @Named("param1Value1")
  @JCUnitParameterValue
  public static String param1Value1(@From("param3") String param3) {
    return "Hello, " + param3;
  }

  @Named
  public static ParameterSpaceSpec parameterSpaceSpec() {
    return ParameterSpaceSpec.create(
        singletonList(
            ParameterSpec.create("param1",
                                 ValueResolver.of("hello"),
                                 ValueResolver.of("world"),
                                 ValueResolver.with(t -> (String) t.get("param3")).$("param3"),
                                 ValueResolver.from(TestExample2.class).classMethod(m -> m.getName().equals("param1Value1")))),
        emptyList());
  }

  @JCUnitTest
  public void test(@From("param1") String param1, @From("param2") Map<String, List<String>> param2) {

  }
}

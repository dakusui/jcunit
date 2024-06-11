package com.github.dakusui.jcunit8.sandbox.example;

import com.github.jcunit.annotations.*;
import com.github.jcunit.core.model.ValueResolver;
import com.github.jcunit.runners.junit5.JCUnitTestExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;

import static com.github.jcunit.core.Invokable.fromClassMethodNamed;
import static com.github.jcunit.core.Invokable.referenceTo;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
@Disabled
@ExtendWith(JCUnitTestExtension.class)
@UsingParameterSpace(TestExample2.ParameterSpace.class)
public class TestExample2 {
  public static class ParameterSpace {
    @Named
    @JCUnitParameter
    public static List<ValueResolver<String>> param1() {
      return asList(ValueResolver.from("hello").$(),
                    ValueResolver.from("world").$(),
                    ValueResolver.fromInvokable(referenceTo("param3", 0)),
                    ValueResolver.fromInvokable(fromClassMethodNamed(ParameterSpace.class, "param1Value1"))
      );
    }

    @Named
    @JCUnitParameter
    public static List<ValueResolver<Map<String, List<String>>>> param2() {
      return asList(ValueResolver.from(singletonMap("K2", singletonList("V2"))).$(),
                    ValueResolver.from(singletonMap("K1", singletonList("V1"))).$());
    }

    @Named("param1Value1")
    @JCUnitParameterValue
    public static String param1Value1(@From("param3") String param3) {
      return "Hello, " + param3;
    }

    @Named
    @JCUnitParameter
    public static List<ValueResolver<String>> param3() {
      return asList(ValueResolver.of("Scott"),
                    ValueResolver.of("John"));
    }

    @Named
    @JCUnitCondition
    public static boolean startingWithSalute(@From("param1") String param1) {
      return param1.startsWith("Hello") ||
             param1.startsWith("hello");
    }
  }

  @JCUnitTest
  @Given("startingWithSalute")
  public void testMethod(@From("param1") String param1, @From("param2") Map<String, List<String>> param2) {
    System.out.println("param1:" + param1 + ", param2:" + param2);
  }
}

package com.github.dakusui.jcunit8.sandbox.example;

import com.github.jcunit.annotations.*;
import com.github.jcunit.annotations.ConfigurePipelineWith.Entry;
import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.model.ValueResolver;
import com.github.jcunit.runners.junit5.JCUnitTestEngine;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@ExtendWith(JCUnitTestEngine.class)
@ConfigurePipelineWith(
    parameterSpaceSpecClass = TestExample6.ParameterSpace.class,
    arguments = {
        @Entry(name = "strength", value = "2"),
        @Entry(name = "negativeTestGeneration", value = "false"),
        @Entry(name = "seedGeneratorMethod", value = {"seed", "seeds"})
    })
public class TestExample6 {
  public static class ParameterSpace {
    @Named
    @JCUnitParameter
    public static List<ValueResolver<String>> param1() {
      return asList(ValueResolver.from("X1").$(),
                    ValueResolver.from("X2").$());
    }

    @Named
    @JCUnitParameter
    public static List<ValueResolver<String>> param2() {
      return asList(ValueResolver.of("Y1"),
                    ValueResolver.of("Y2"));
    }

    @Named
    @JCUnitSeedGenerator
    public static Tuple seed() {
      return Tuple.builder()
                  .put("param1", singletonList(ValueResolver.of("Xa")))
                  .put("param2", singletonList(ValueResolver.of("Ya")))
                  .build();
    }

    @Named
    @JCUnitSeedGenerator
    public static Tuple[] seeds() {
      return new Tuple[]{
          Tuple.builder()
               .put("param1", singletonList(ValueResolver.of("Xb")))
               .put("param2", singletonList(ValueResolver.of("Yb")))
              .build()
      };
    }
  }

  @JCUnitTest
  public void testMethodNegative(@From("param1") String param1, @From("param2") String param2) {
    System.out.println("param1:" + param1 + ",param2:" + param2);
  }
}

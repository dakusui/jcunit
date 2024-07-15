package com.github.dakusui.jcunit8.sandbox.example;

import com.github.dakusui.jcunit8.testutils.TestBase;
import com.github.jcunit.annotations.*;
import com.github.jcunit.factorspace.Range;
import com.github.jcunit.model.ValueResolver;
import com.github.jcunit.runners.junit5.JCUnitTestEngine;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static com.github.jcunit.core.Invokable.referenceTo;
import static java.util.Arrays.asList;

@ExtendWith(JCUnitTestEngine.class)
@ConfigureWith(parameterSpace = TestExample4.ParameterSpace.class)
public class TestExample4 extends TestBase /* Extending TestBase just to suppress writes to stdout/err during CI */ {
  public static class ParameterSpace {
    @Named
    @JCUnitParameter
    public static List<ValueResolver<String>> param1() {
      return asList(ValueResolver.from("X1").$(),
                    ValueResolver.fromInvokable(referenceTo("param2", Range.of("0"))));
    }

    @Named
    @JCUnitParameter
    public static List<ValueResolver<String>> param2() {
      return asList(ValueResolver.of("Y1"),
                    ValueResolver.of("Y2"));
    }
  }

  @JCUnitTest
  public void testMethod(@From("param1") String param1) {
    System.out.println("param1:" + param1);
  }
}

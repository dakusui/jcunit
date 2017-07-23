package com.github.dakusui.jcunit8.tests.features.seed;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.ConfigFactory;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ConfigureWith;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import com.github.dakusui.jcunit8.testutils.ResultUtils;
import com.github.dakusui.jcunit8.testutils.UTUtils;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static com.github.dakusui.jcunit8.testutils.UTUtils.matcher;
import static java.util.Arrays.asList;
import static org.junit.Assert.fail;

public class SeedTest {
  @RunWith(JCUnit8.class)
  @ConfigureWith(SeedsForSimpleParameters.Config.class)
  public static class SeedsForSimpleParameters {
    public static class Config extends ConfigFactory.Base {
      @Override
      protected Requirement defineRequirement(Requirement.Builder defaultValues) {
        return defaultValues.addSeed(
            Tuple.builder().put("a", 0).put("b", 0).put("c", 0).build()
        ).addSeed(
            Tuple.builder().put("a", 1).put("b", 1).put("c", 1).build()
        ).build();
      }
    }

    @ParameterSource
    public Parameter.Simple.Factory<Integer> a() {
      return Parameter.Simple.Factory.of(asList(0, 1));
    }

    @ParameterSource
    public Parameter.Simple.Factory<Integer> b() {
      return Parameter.Simple.Factory.of(asList(0, 1));
    }

    @ParameterSource
    public Parameter.Simple.Factory<Integer> c() {
      return Parameter.Simple.Factory.of(asList(0, 1));
    }

    @Test
    public void test(
        @From("a") int a,
        @From("b") int b,
        @From("c") int c
    ) {
      String msg = String.format("a=%d,b=%d,c=%d%n", a, b, c);
      System.out.println(msg);
      fail(msg);
    }
  }

  @Test
  public void simpleSeeds() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(SeedsForSimpleParameters.class),
        matcher(
            UTUtils.oracle("was not successful", result -> !result.wasSuccessful()),
            UTUtils.oracle(
                "{x}.getRunCount()", Result::getRunCount,
                "==5", v -> v == 5
            ),
            UTUtils.oracle(
                "{x}.getFailureCount()", Result::getFailureCount,
                "==5", v -> v == 5
            ),
            UTUtils.oracle(
                "{x}.getFailures().get(0).getMessage()", result -> result.getFailures().get(0).getMessage(),
                "contains'a=0,b=0,c=0'", v -> v.contains("a=0,b=0,c=0")
            )
        ));
  }
}

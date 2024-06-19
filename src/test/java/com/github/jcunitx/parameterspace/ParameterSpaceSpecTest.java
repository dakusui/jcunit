package com.github.jcunitx.parameterspace;

import com.github.dakusui.jcunit8.testutils.TestBase;
import com.github.dakusui.jcunit8.ututiles.PipelineConfigBuilder;
import com.github.jcunit.factorspace.Constraint;
import com.github.jcunit.factorspace.ParameterSpace;
import com.github.jcunit.model.ParameterSpaceSpec;
import com.github.jcunit.model.ParameterSpec;
import com.github.jcunit.model.ValueResolver;
import com.github.jcunit.pipeline.Pipeline;
import com.github.jcunit.pipeline.PipelineSpec;
import com.github.jcunit.testsuite.TestSuite;
import com.github.valid8j.pcond.forms.Printables;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static com.github.valid8j.fluent.Expectations.assertStatement;
import static com.github.valid8j.fluent.Expectations.value;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
public class ParameterSpaceSpecTest extends TestBase {
  @Test
  public void testParameterSpaceSpec() {
    List<ParameterSpec<?>> parameterSpecs = parameterParameterSpecs();
    List<Constraint> constraints = parameterConstraints();
    {
      ParameterSpaceSpec spaceSpec = ParameterSpaceSpec.create(parameterSpecs, constraints);
      {
        int parameterSpecsSize = parameterSpecs.size();
        ParameterSpec<?> firstParameterSpec = parameterSpecs.get(0);
        ParameterSpec<?> lastParameterSpec = parameterSpecs.get(parameterSpecsSize - 1);
        {
          assertStatement(value(spaceSpec).satisfies(spec -> spec.function(functionParameterNames())
                                                                 .asListOf(String.class)
                                                                 .satisfies(names -> names.size()
                                                                                          .toBe()
                                                                                          .equalTo(parameterSpecsSize))
                                                                 .satisfies(names -> names.elementAt(0)
                                                                                          .satisfies()
                                                                                          .equalTo(firstParameterSpec.name()))
                                                                 .satisfies(names -> names.elementAt(parameterSpecsSize - 1)
                                                                                          .toBe()
                                                                                          .equalTo(lastParameterSpec.name())))
                                          .satisfies(spec -> spec.function(functionParameterSpecFor(firstParameterSpec.name()))
                                                                 .function(functionValueResolvers())
                                                                 .asList()
                                                                 .size()
                                                                 .toBe()
                                                                 .equalTo(firstParameterSpec.valueResolvers().size()))
                                          .satisfies(spec -> spec.function(functionParameterSpecFor(lastParameterSpec.name()))
                                                                 .function(functionValueResolvers())
                                                                 .asList()
                                                                 .size()
                                                                 .toBe()
                                                                 .equalTo(lastParameterSpec.valueResolvers().size())));
        }
      }
    }
  }

  @Test
  public void testToParameterSpace() {
    List<ParameterSpec<?>> parameterSpecs = parameterParameterSpecs();
    List<Constraint> constraints = parameterConstraints();
    Function<ParameterSpaceSpec, List<String>> parameterSeedParameterNamesGenerator = parameterSeedParameterNamesGenerator();
    {
      ParameterSpaceSpec testParameterSpaceSpec = ParameterSpaceSpec.create(parameterSpecs, constraints);
      List<String> seedParameterNames = parameterSeedParameterNamesGenerator.apply(testParameterSpaceSpec);
      {
        ParameterSpace parameterSpace = testParameterSpaceSpec.toParameterSpace(seedParameterNames);
        {
          int parameterSpecsSize = parameterSpecs.size();
          ParameterSpec<?> firstParameterSpec = parameterSpecs.get(0);
          ParameterSpec<?> lastParameterSpec = parameterSpecs.get(parameterSpecsSize - 1);
          {
            assertStatement(value(parameterSpace).satisfies(s -> s.function(functionGetParameterNames())
                                                                  .asListOf(String.class)
                                                                  .elementAt(0)
                                                                  .toBe()
                                                                  .equalTo(firstParameterSpec.name()))
                                                 .satisfies(s -> s.function(functionGetParameterNames())
                                                                  .asListOf(String.class)
                                                                  .elementAt(parameterSpecsSize - 1)
                                                                  .toBe()
                                                                  .equalTo(lastParameterSpec.name())));
          }
        }
      }
    }
  }

  @Test
  public void testToParameterSpaceEmpty() {
    List<ParameterSpec<?>> parameterSpecs = parameterParameterSpecs_empty();
    List<Constraint> constraints = parameterConstraints();
    Function<ParameterSpaceSpec, List<String>> parameterSpaceSpecListFunction = parameterSeedParameterNamesGenerator();
    {
      ParameterSpaceSpec testParameterSpaceSpec = ParameterSpaceSpec.create(parameterSpecs, constraints);
      List<String> seedParameterNames = parameterSpaceSpecListFunction.apply(testParameterSpaceSpec);
      {
        ParameterSpace parameterSpace = testParameterSpaceSpec.toParameterSpace(seedParameterNames);
        {
          assertStatement(value(parameterSpace).satisfies(s -> s.function(functionGetParameterNames())
                                                                .asListOf(String.class)
                                                                .toBe()
                                                                .empty()));
        }
      }
    }
  }

  @Test
  public void testToTestSuite() {
    List<ParameterSpec<?>> parameterSpecs = parameterParameterSpecs();
    List<Constraint> constraints = parameterConstraints();
    boolean negativeTestGenerationEnabled = parameterNegativeTestGenerationEnabled();
    Function<ParameterSpaceSpec, List<String>> parameterFunctionParamaterSpaceSpecToSeedParameterNames = parameterSeedParameterNamesGenerator();
    {
      ParameterSpaceSpec testParameterSpaceSpec = ParameterSpaceSpec.create(parameterSpecs, constraints);
      PipelineSpec pipelineSpec = new PipelineSpec.Builder(new PipelineConfigBuilder().withNegativeTestGeneration(negativeTestGenerationEnabled)
                                                                                      .build())
          .build();
      List<String> seedParameterNames = parameterFunctionParamaterSpaceSpecToSeedParameterNames.apply(testParameterSpaceSpec);
      {
        TestSuite testSuite = Pipeline.Standard.create(pipelineSpec).execute(testParameterSpaceSpec.toParameterSpace(seedParameterNames));
        {
          testSuite.forEach(System.out::println);
        }
      }
    }
  }

  private static boolean parameterNegativeTestGenerationEnabled() {
    return false;
  }

  private static Function<ParameterSpaceSpec, List<String>> parameterSeedParameterNamesGenerator() {
    return ParameterSpaceSpecTest::parameterSeedParameterNames;
  }

  private static List<ParameterSpec<?>> parameterParameterSpecs_empty() {
    return emptyList();
  }

  private static List<ParameterSpec<?>> parameterParameterSpecs() {
    return asList(SpecTestUtils.createTestParameterSpecP1(),
                  SpecTestUtils.createTestParameterSpecP2());
  }

  private static List<String> parameterSeedParameterNames(ParameterSpaceSpec testParameterSpaceSpec) {
    return testParameterSpaceSpec.parameterNames();
  }

  private static List<Constraint> parameterConstraints() {
    return emptyList();
  }

  private static Function<ParameterSpaceSpec, List<String>> functionParameterNames() {
    return Printables.function("parameterNames", ParameterSpaceSpec::parameterNames);
  }

  private static Function<ParameterSpaceSpec, ParameterSpec<Object>> functionParameterSpecFor(String parameterSpecName) {
    return Printables.function("parameterSpecFor[" + parameterSpecName + "]", s -> s.parameterSpecFor(parameterSpecName));
  }

  private static Function<ParameterSpace, List<String>> functionGetParameterNames() {
    return Printables.function("getParameterNames", ParameterSpace::getParameterNames);
  }

  private static Function<ParameterSpec<Object>, List<ValueResolver<Object>>> functionValueResolvers() {
    return Printables.function("valueResolvers", ParameterSpec::valueResolvers);
  }
}

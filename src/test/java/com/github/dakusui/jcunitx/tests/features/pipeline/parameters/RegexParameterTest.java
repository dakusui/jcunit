package com.github.dakusui.jcunitx.tests.features.pipeline.parameters;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.factorspace.Constraint;
import com.github.dakusui.jcunitx.metamodel.parameters.RegexParameter;
import com.github.dakusui.jcunitx.metamodel.parameters.SimpleParameter;
import com.github.dakusui.jcunitx.pipeline.stages.generators.IpoGplus;
import com.github.dakusui.jcunitx.testsuite.SchemafulAArraySet;
import com.github.dakusui.jcunitx.testsuite.TestCase;
import com.github.dakusui.jcunitx.testutils.*;
import org.junit.Test;

import java.util.List;
import java.util.Objects;

import static com.github.dakusui.jcunitx.testutils.UTUtils.allSatisfy;
import static com.github.dakusui.jcunitx.testutils.UTUtils.sizeIs;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class RegexParameterTest extends PipelineTestBase {
  @Test
  public void whenBuildFactorSpace() {
    //noinspection PointlessArithmeticExpression
    FactorSpaceUtils.validateFactorSpace(
        RegexParameter.Descriptor.of("A(B|C){0,3}").create("regex1").toFactorSpace(),
        UTUtils.matcher(
            FactorSpaceUtils.sizeOfFactorsIs(
                "==10[(3times + 1) + (2times + 1) + (1time + 1) + (0time + 1)]",
                (Integer size) -> size == (3 + 1) + (2 + 1) + (1 + 1) + (0 + 1)
            ),
            FactorSpaceUtils.sizeOfConstraintsIs(
                ">0",
                (Integer size) -> size > 0
            )));
  }

  @Test
  public void givenRegex$whenGenerateWithIpoG$thenNonEmptyTupleSetGenerated() {
    SchemafulTupleSetUtils.validateSchemafulTupleSet(
        SchemafulAArraySet.fromRows(
            new IpoGplus(
                RegexParameter.Descriptor.of("A(B|C){0,3}").create("regex1").toFactorSpace(),
                requirement(),
                emptyList()
            ).generate()),
        UTUtils.matcherFromPredicates(
            sizeIs(">0", value -> value > 0),
            allSatisfy(UTUtils.oracle("not empty", tuple -> !tuple.isEmpty())),
            allSatisfy(UTUtils.oracle("starts with REGEX:regex1", tuple -> tuple.keySet().stream().allMatch(key -> key.startsWith("REGEX:regex1:"))))
        ));
  }


  @Test
  public void givenRegex$whenBuildTestSuite$thenNonEmptyTestSuiteBuilt() {
    TestSuiteUtils.validateTestSuite(
        generateTestSuite(
            RegexParameter.Descriptor.of("A(B|C){0,3}").create("regex1")
        ),
        UTUtils.matcherFromPredicates(
            sizeIs(
                "1+2+4+4(possible smallest)<=size<1+2+4+8(acceptable largest)",
                value -> value >= 1 + 2 + 4 + 4 && value < 1 + 2 + 4 + 8
            ),
            allSatisfy(UTUtils.oracle(
                "Non empty",
                (TestCase t) -> !t.getTestInput().isEmpty()
            )),
            allSatisfy(UTUtils.oracle(
                "'regex1' attribute holds a non-empty list",
                (TestCase t) ->
                    t.getTestInput().get("regex1") instanceof List && !((List<?>) t.getTestInput().get("regex1")).isEmpty()
            )),
            allSatisfy(UTUtils.oracle(
                "First element of 'regex1' attribute is 'A'",
                (TestCase t) -> Objects.equals(((List<?>) t.getTestInput().get("regex1")).get(0), "A")
            ))));
  }

  @Test
  public void whenPreprocess$thenNonSimpleParameterInvolvedInConstraintWillBeRemoved() {
    ParameterSpaceUtils.validateParameterSpace(
        preprocess(RegexParameter.Descriptor.of("A(B|C){0,3}").create("regex1")),
        UTUtils.matcher(
            ParameterSpaceUtils.hasParameters(1),
            ParameterSpaceUtils.parametersAreAllInstancesOf(RegexParameter.class),
            ParameterSpaceUtils.hasConstraints(0)
        ));
  }

  @Test
  public void whenPreprocessWithConstraints$thenNonSimpleParameterInvolvedInConstraintWillBeRemoved() {
    ParameterSpaceUtils.validateParameterSpace(
        preprocess(
            singletonList(RegexParameter.Descriptor.of("A(B|C){0,3}").create("regex1")),
            singletonList(Constraint.create(
                "alwaysTrue[regex1]",
                tuple -> true,
                "regex1"
            ))),
        UTUtils.matcher(
            ParameterSpaceUtils.hasParameters(1),
            ParameterSpaceUtils.parametersAreAllInstancesOf(SimpleParameter.class),
            ParameterSpaceUtils.hasConstraints(1)
        ));
  }

  @Test
  public void whenEngine$thenSchemafulTupleSetGenerated() {
    SchemafulTupleSetUtils.validateSchemafulTupleSet(
        engine(
            singletonList(RegexParameter.Descriptor.of("A(B|C){0,3}").create("regex1")),
            emptyList()),
        UTUtils.matcherFromPredicates(
            sizeIs(
                "1+2+4+4(possible smallest)<=size<1+2+4+8(acceptable largest)",
                (Integer value) -> value >= 1 + 2 + 4 + 4 && value < 1 + 2 + 4 + 8
            ),
            allSatisfy(
                (AArray tuple) -> {
                  Object value = tuple.get("regex1");
                  if (!(value instanceof List))
                    return false;
                  List<?> list = (List<?>) value;
                  return list.size() >= 1 && list.size() <= 4;
                }
            )));
  }

  @Test
  public void whenEncode$thenFactorSpaceCreated() {
    FactorSpaceUtils.validateFactorSpace(
        encode(
            ////
            // Currently, inner 'choice' operator ('|') is expanded only once,
            // even if there is a 'repetition' operator ('{n,m}') outside.
            singletonList(RegexParameter.Descriptor.of("A((B|C)D){0,3}").create("regex1")),
            emptyList()
        ),
        UTUtils.matcher(
            FactorSpaceUtils.sizeOfFactorsIs(
                "==1(choice)+3(each repetition)+1(choice for repetition)+1(top level)",
                value -> value == 1 + 3 + 1 + 1
            ),
            FactorSpaceUtils.sizeOfConstraintsIs(
                "==5",
                value -> value == 5
            )));
  }
}

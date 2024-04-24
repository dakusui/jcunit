package com.github.dakusui.jcunit8.tests.features.pipeline.constraints;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.factorspace.ParameterSpace;
import com.github.dakusui.jcunit8.pipeline.stages.generators.Cartesian;
import com.github.dakusui.jcunit8.pipeline.stages.generators.IpoGplus;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testutils.ParameterSpaceUtils;
import com.github.dakusui.jcunit8.testutils.PipelineTestBase;
import com.github.dakusui.jcunit8.testutils.SchemafulTupleSetUtils;
import com.github.dakusui.jcunit8.testutils.TestSuiteUtils;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.dakusui.jcunit8.testutils.UTUtils.matcher;
import static com.github.dakusui.jcunit8.testutils.UTUtils.oracle;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class ImpossibleConstraintTest extends PipelineTestBase {
  @Test
  public void test() {
    TestSuiteUtils.validateTestSuite(
        generateTestSuite(
            asList(
                simpleParameterFactoryWithDefaultValues().create("simple1"),
                simpleParameterFactoryWithDefaultValues().create("simple2"),
                simpleParameterFactoryWithDefaultValues().create("simple3")
            ),
            Collections.singletonList(
                Constraint.create("alwaysTrue[simple1]", (Tuple tuple) -> false, "simple1") // Never becomes true
            )
        ),
        matcher(
            oracle("size of ", List::size, "==0", value -> value == 0)
        )
    );
  }

  @Test
  public void whenGenerateSchemafulTupleSet() {
    SchemafulTupleSetUtils.validateSchemafulTupleSet(
        engine(
            asList(
                simpleParameterFactoryWithDefaultValues().create("simple1"),
                simpleParameterFactoryWithDefaultValues().create("simple2"),
                simpleParameterFactoryWithDefaultValues().create("simple3")
            ),
            Collections.singletonList(
                Constraint.create("alwaysTrue[simple1]", (Tuple tuple) -> false, "simple1") // Never becomes true
            )
        ),
        matcher(
            oracle("{x}.isEmpty()", List::isEmpty)
        )
    );
  }

  @Test
  public void givenSimpleParametersAndImpossibleConstraint$whenPreprocess$thenParameterSpaceIsStillPrepared() {
    ParameterSpaceUtils.validateParameterSpace(
        preprocess(
            asList(
                simpleParameterFactoryWithDefaultValues().create("simple1"),
                simpleParameterFactoryWithDefaultValues().create("simple2"),
                simpleParameterFactoryWithDefaultValues().create("simple3")
            ),
            Collections.singletonList(
                Constraint.create("alwaysTrue[simple1]", (Tuple tuple) -> false, "simple1") // Never becomes true
            )
        ),
        matcher(
            oracle(
                "{x}.getParameterNames()",
                ParameterSpace::getParameterNames,
                "==[simple1,simple2,simple3]",
                v -> v.equals(asList("simple1", "simple2", "simple3"))
            ),
            oracle(
                "{x}.getConstraints().size()",
                parameterSpace -> parameterSpace.getConstraints().size(),
                "==1",
                v -> v == 1)
        )
    );
  }

  @Test
  public void givenImpossibleConstraint$whenGenerateWithIpoGplus$thenEmptyTupleSetGenerated() {
    FactorSpace factorSpace = buildSimpleFactorSpaceWithImpossibleConstraint();
    SchemafulTupleSetUtils.validateSchemafulTupleSet(
        new SchemafulTupleSet.Builder(
            factorSpace.getFactors().stream().map(Factor::getName).collect(Collectors.toList())
        ).addAll(
            new IpoGplus(
                factorSpace,
                requirement(),
                emptyList()
            ).generate()
        ).build(),
        matcher(
            oracle("Generated tupleSet is empty", List::isEmpty)
        )
    );
  }

  @Test
  public void givenImpossibleConstraint$whenGenerateWithCartesian$thenExceptionThrown() {
    FactorSpace factorSpace = buildSimpleFactorSpaceWithImpossibleConstraint();
    SchemafulTupleSetUtils.validateSchemafulTupleSet(
        new SchemafulTupleSet.Builder(
            factorSpace.getFactors().stream().map(Factor::getName).collect(Collectors.toList())
        ).addAll(
            new Cartesian(
                factorSpace,
                requirement()).generate()
        ).build(),
        matcher(
            oracle("Generated tupleSet is empty", List::isEmpty)
        )
    );
  }
}

package com.github.dakusui.jcunit8.tests.features.pipeline.constraints;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.stages.generators.Cartesian;
import com.github.dakusui.jcunit8.pipeline.stages.generators.IpoGplus;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testutils.*;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

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
                Constraint.create((Tuple tuple) -> false, "simple1") // Never becomes true
            )
        ),
        UTUtils.matcher(
            UTUtils.oracle("size of ", List::size, "==0", value -> value == 0)
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
                Constraint.create((Tuple tuple) -> false, "simple1") // Never becomes true
            )
        ),
        UTUtils.matcher()
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
                Constraint.create((Tuple tuple) -> false, "simple1") // Never becomes true
            )
        ),
        UTUtils.matcher(

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
                Collections.emptyList(),
                factorSpace,
                requirement()).generate()
        ).build(),
        UTUtils.matcher(
            UTUtils.oracle("Generated tupleSet is empty", List::isEmpty)
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
                Collections.emptyList(),
                factorSpace,
                requirement()).generate()
        ).build(),
        UTUtils.matcher(
            UTUtils.oracle("Generated tupleSet is empty", List::isEmpty)
        )
    );
  }
}

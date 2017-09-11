package com.github.dakusui.jcunit8.tests.features.pipeline.parameters;

import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.pipeline.stages.generators.IpoGplus;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testutils.*;
import org.junit.Test;

import java.util.List;

import static com.github.dakusui.jcunit8.testutils.UTUtils.sizeIs;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class CustomParameterTest extends PipelineTestBase {
  @Test
  public void givenOneCustomParameter$whenBuildFactorSpace$thenBuilt() {
    FactorSpaceUtils.validateFactorSpace(
        customParameterFactory().create("custom1").toFactorSpace(),
        UTUtils.matcher(
            UTUtils.oracle(
                "number of factors", value -> value.getFactors().size(),
                "==2", value -> value == 2)
        )
    );
  }

  @Test
  public void givenOneCustomParameter$whenGenerateWithIpoG$thenTupleSetGenerated() {
    SchemafulTupleSetUtils.validateSchemafulTupleSet(
        SchemafulTupleSet.fromTuples(
            new IpoGplus(
                customParameterFactory().create("custom1").toFactorSpace(),
                requirement(),
                emptyList()
            ).generate()),
        UTUtils.matcher(
            UTUtils.oracle(
                "size", List::size,
                ">3", size -> size > 3)
        )
    );
  }

  @Test
  public void givenOneCustomParameter$whenBuildTestSuite$thenBuilt() {
    TestSuiteUtils.validateTestSuite(
        generateTestSuite(
            customParameterFactory().create("custom1")
        ),
        UTUtils.matcher(
            // Custom parameter should generate more than 3 tests
            UTUtils.oracle(
                "size", List::size,
                ">3", size -> size > 3)
        )
    );
  }

  @Test
  public void givenOneSimpleAndOneCustomParameters$whenBuildTestSuite$thenBuilt() {
    TestSuiteUtils.validateTestSuite(
        generateTestSuite(
            customParameterFactory().create("custom1"),
            simpleParameterFactoryWithDefaultValues().create("simple1")
        ),
        UTUtils.matcher(
            // Custom parameter should generate more than 3 tests
            UTUtils.oracle(
                "size", List::size,
                ">3", size -> size > 3)
        ));
  }

  @Test
  public void whenPreprocess$thenNonSimpleParameterInvolvedInConstraintWillBeSimplified() {
    ParameterSpaceUtils.validateParameterSpace(
        preprocess(
            asList(
                customParameterFactory().create("custom1"),
                simpleParameterFactory("V1", "V2").create("simple1")
            ),
            singletonList(
                Constraint.create("alwaysTrue[custom1]", tuple -> true, "custom1")
            )
        ),
        UTUtils.matcher(
            ParameterSpaceUtils.hasParameters(2),
            ParameterSpaceUtils.parametersAreAllInstancesOf(Parameter.Simple.class),
            ParameterSpaceUtils.hasConstraints(1)
        )
    );
  }

  @Test
  public void givenOneCustomParameterWithConstraints$whenPreprocess$thenNonSimpleParameterInvolvedInConstraintWillBeRemoved() {
    ParameterSpaceUtils.validateParameterSpace(
        preprocess(
            singletonList(customParameterFactory().create("custom1")),
            singletonList(
                Constraint.create(
                    "alwaysTrue[custom1]",
                    tuple -> true,
                    "custom1"
                )
            )),
        UTUtils.matcher(
            ParameterSpaceUtils.hasParameters(1),
            ParameterSpaceUtils.parametersAreAllInstancesOf(Parameter.Simple.class),
            ParameterSpaceUtils.sizeOfParameterKnownValuesSatisfies("custom1", value -> value > 0),
            ParameterSpaceUtils.hasConstraints(1)
        )
    );
  }

  @Test
  public void givenCustomParameter$whenEngine$thenSchemafulTupleSetGenerated() {
    SchemafulTupleSetUtils.validateSchemafulTupleSet(
        engine(
            singletonList(customParameterFactory().create("custom1")),
            emptyList()
        ),
        UTUtils.matcher(
            // Custom parameter should generate more than 3 tests
            sizeIs(">3", value -> value > 3)
        )
    );
  }


  @Test
  public void given1Simple1CustomParameters$whenEngine$thenSchemafulTupleSetGenerated() {
    SchemafulTupleSetUtils.validateSchemafulTupleSet(
        engine(
            asList(customParameterFactory().create("custom1"), simpleParameterFactoryWithDefaultValues().create("simple1")),
            emptyList()
        ),
        UTUtils.matcher(
            /*TODO
            sizeIs(
                // Custom parameter should generate more than 3 tests
                oracle(">3", value -> value > 3)
            )
            */
        )
    );
  }
}

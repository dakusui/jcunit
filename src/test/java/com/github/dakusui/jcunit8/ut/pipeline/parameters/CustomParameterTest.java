package com.github.dakusui.jcunit8.ut.pipeline.parameters;

import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.pipeline.stages.generators.IpoG;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.ut.pipeline.testbase.*;
import org.junit.Test;

import static com.github.dakusui.jcunit8.ut.pipeline.testbase.UTUtils.sizeIs;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class CustomParameterTest extends PipelineTestBase {
  @Test
  public void givenOneCustomParameter$whenBuildFactorSpace$thenBuilt() {
    FactorSpaceUtils.validateFactorSpace(
        customParameterFactory().create("custom1").toFactorSpace(),
        matcher(
            FactorSpaceUtils.sizeOfFactorsIs(name("==2", value -> value == 2))
        )
    );
  }

  @Test
  public void givenOneCustomParameter$whenGenerateWithIpoG$thenTupleSetGenerated() {
    SchemafulTupleSetUtils.validateSchemafulTupleSet(
        SchemafulTupleSet.fromTuples(
            new IpoG(
                emptyList(),
                customParameterFactory().create("custom1").toFactorSpace(),
                requirement()
            ).generate()),
        matcher(
            sizeIs(
                // Custom parameter should generate more than 3 tests
                name(">3", integer -> integer > 3)
            )
        )
    );
  }

  @Test
  public void givenOneCustomParameter$whenBuildTestSuite$thenBuilt() {
    TestSuiteUtils.validateTestSuite(
        generateTestSuite(
            customParameterFactory().create("custom1")
        ),
        matcher(
            sizeIs(
                // Custom parameter should generate more than 3 tests
                name(">3", value -> value > 3)
            )
        ));
  }

  @Test
  public void givenOneSimpleAndOneCustomParameters$whenBuildTestSuite$thenBuilt() {
    TestSuiteUtils.validateTestSuite(
        generateTestSuite(
            customParameterFactory().create("custom1"),
            simpleParameterFactoryWithDefaultValues().create("simple1")
        ),
        matcher(
            sizeIs(
                // Custom parameter should generate more than 3 tests
                name(">3", value -> value > 3)
            )
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
                Constraint.create(tuple -> true, "custom1")
            )
        ),
        matcher(
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
                    tuple -> true,
                    "custom1"
                )
            )),
        matcher(
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
        matcher(
            sizeIs(
                // Custom parameter should generate more than 3 tests
                name(">3", value -> value > 3)
            )
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
        matcher(
            sizeIs(
                // Custom parameter should generate more than 3 tests
                name(">3", value -> value > 3)
            )
        )
    );
  }
}

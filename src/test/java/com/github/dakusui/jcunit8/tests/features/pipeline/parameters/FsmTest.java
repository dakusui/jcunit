package com.github.dakusui.jcunit8.tests.features.pipeline.parameters;

import com.github.dakusui.jcunit8.examples.flyingspaghettimonster.FlyingSpaghettiMonsterSpec;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.factorspace.ParameterSpace;
import com.github.dakusui.jcunit8.pipeline.Config;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.generators.Cartesian;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testutils.*;
import org.junit.Test;

import java.util.function.Function;

import static com.github.dakusui.jcunit8.testutils.UTUtils.sizeIs;
import static java.util.Collections.*;

public class FsmTest extends PipelineTestBase {
  @Test
  public void givenFlyingSpaghttiMonster$whenBuildFactorSpace$thenBuilt() {
    FactorSpaceUtils.validateFactorSpace(
        Parameter.Fsm.Factory.of(FlyingSpaghettiMonsterSpec.class, 2).create("fsm1").toFactorSpace(),
        matcher(
            FactorSpaceUtils.sizeOfFactorsIs(name("==6", value -> value == 6)),
            FactorSpaceUtils.sizeOfConstraintsIs(name("==8", value -> value == 8))
        )
    );
  }

  @Test
  public void givenFlyingSpaghettiMonster$whenGenerateWithCartesian$thenGenerated() {
    SchemafulTupleSetUtils.validateSchemafulTupleSet(
        SchemafulTupleSet.fromTuples(
            new Cartesian(
                emptyList(),
                Parameter.Fsm.Factory.of(FlyingSpaghettiMonsterSpec.class, 1)
                    .create("fsm1")
                    .toFactorSpace(),
                requirement()
            ).generate()
        ),
        matcher(
            sizeIs(name(">=number of states, at least", size -> size >= FlyingSpaghettiMonsterSpec.values().length))
        )
    );
  }

  @Test
  public void givenFlyingSpaghettiMonster$whenGenerateTestSuite$thenGenerated() {
    TestSuiteUtils.validateTestSuite(
        generateTestSuite(
            Parameter.Fsm.Factory.of(FlyingSpaghettiMonsterSpec.class, 1).create("fsm1")
        ),
        matcher(
            sizeIs(name(">=number of states, at least", size -> size >= FlyingSpaghettiMonsterSpec.values().length))
        )
    );
  }

  @Test
  public void givenFlyingSpaghettiMonster$whenGenerateTestSuiteFromFactorsWithConstraints() {
    TestSuiteUtils.validateTestSuite(
        generateTestSuite(
            singletonList(Parameter.Fsm.Factory.of(FlyingSpaghettiMonsterSpec.class, 1).create("fsm1")),
            singletonList(Constraint.create(tuple -> true, "fsm1"))
        ),
        matcher(
            sizeIs(name(">=number of states, at least", size -> size >= FlyingSpaghettiMonsterSpec.values().length))
        )
    );
  }

  @Test
  public void givenFlyingSpaghettiMonster$whenEncodeParameterSpace$thenFactorSpaceGenerated() {
    Function<ParameterSpace, FactorSpace> encoder = Config.Builder.forTuple(new Requirement.Builder().withNegativeTestGeneration(false).build()).build().encoder();
    FactorSpaceUtils.validateFactorSpace(
        encoder.apply(
            new ParameterSpace.Builder()
                .addAllParameters(singleton(Parameter.Fsm.Factory.of(FlyingSpaghettiMonsterSpec.class, 1).create("fsm1")))
                .build()
        ),
        matcher(
            FactorSpaceUtils.sizeOfFactorsIs(name("==6", value -> value == 3)),
            FactorSpaceUtils.sizeOfConstraintsIs(name("==8", value -> value == 4))
        )
    );
  }

  @Test
  public void givenFlyingSpaghettiMonsteWithNoConstraint$whenPreprocess$thenFsmParameterWillBeKept() {
    ParameterSpaceUtils.validateParameterSpace(
        preprocess(
            singletonList(
                Parameter.Fsm.Factory.of(FlyingSpaghettiMonsterSpec.class, 1).create("fsm1")),
            emptyList()
        ),
        matcher(
            ParameterSpaceUtils.hasParameters(1),
            ParameterSpaceUtils.parameterIsInstanceOf("fsm1", Parameter.Fsm.class),
            ParameterSpaceUtils.hasConstraints(0)
        )
    );
  }

  @Test
  public void givenFlyingSpaghettiMonsterWithConstraint$whenPreprocess$thenParameterSpaceWithSimpleParameterAndConstraintIsGenerated() {
    ParameterSpaceUtils.validateParameterSpace(
        preprocess(
            singletonList(Parameter.Fsm.Factory.of(FlyingSpaghettiMonsterSpec.class, 1).create("fsm1")),
            singletonList(Constraint.create(tuple -> true, "fsm1"))
        ),
        matcher(
            ParameterSpaceUtils.hasParameters(1),
            ParameterSpaceUtils.parameterIsInstanceOf("fsm1", Parameter.Simple.class),
            ParameterSpaceUtils.sizeOfParameterKnownValuesSatisfies(
                "fsm1",
                name(">0",
                    size -> size > 0)
            ),
            ParameterSpaceUtils.hasConstraints(1)
        )
    );
  }
}

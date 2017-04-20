package com.github.dakusui.jcunit8.tests.features.pipeline.stages;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.tests.features.pipeline.testbase.CustomParameter;
import com.github.dakusui.jcunit8.tests.features.pipeline.testbase.PipelineTestBase;
import org.junit.Test;

import static com.github.dakusui.jcunit8.tests.features.pipeline.testbase.ParameterSpaceUtils.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class PreprocessTest extends PipelineTestBase {
  @Test
  public void givenOneSimpleParameter$whenPreprocess$thenParameterSpaceWithOneSimpleParameterIsReturned() {
    validateParameterSpace(
        preprocess(simpleParameterFactory("default", "value").create("simple1")),
        matcher(
            hasParameters(1),
            parametersAreAllInstancesOf(Parameter.Simple.class),
            knownValuesOfParameterAre("simple1", "default", "value"),
            hasConstraints(0)
        ));
  }

  @Test
  public void givenTwoSimpleParameters$whenPreprocess$thenParameterSpaceWithTwoSimpleParametersIsReturned() {
    validateParameterSpace(
        preprocess(
            simpleParameterFactory("default", "value1").create("simple1"),
            simpleParameterFactory("default", "value2").create("simple2")
        ),
        matcher(
            hasParameters(2),
            parametersAreAllInstancesOf(Parameter.Simple.class),
            knownValuesOfParameterAre("simple1", "default", "value1"),
            knownValuesOfParameterAre("simple2", "default", "value2"),
            hasConstraints(0)
        )
    );
  }

  @Test
  public void givenSingleSimpleParameterUnderOneConstraint$whenPreprocess$thenParameterAndConstraintAreKept() {
    validateParameterSpace(
        preprocess(
            singletonList(simpleParameterFactory("default", "value1").create("simple1")),
            singletonList(Constraint.create(tuple -> true, "simple1"))
        ),
        matcher(
            hasParameters(1),
            parametersAreAllInstancesOf(Parameter.Simple.class),
            knownValuesOfParameterAre("simple1", "default", "value1"),
            hasConstraints(1)
        ));
  }

  @Test
  public void givenTwoSimpleParameterUnderOneConstraint$whenPreprocess$thenParametersAndConstraintAreKept() {
    validateParameterSpace(
        preprocess(
            asList(
                simpleParameterFactory("default", "value1").create("simple1"),
                simpleParameterFactory("default", "value2").create("simple2")
            ),
            singletonList(Constraint.create((Tuple tuple) -> true, "simple1"))
        ),
        matcher(
            hasParameters(2),
            parametersAreAllInstancesOf(Parameter.Simple.class),
            knownValuesOfParameterAre("simple1", "default", "value1"),
            knownValuesOfParameterAre("simple2", "default", "value2"),
            hasConstraints(1)
        )
    );
  }

  @Test
  public void givenCustomParameterWithNoConstraint$whenPreprocess$thenCustomParameterKept() {
    validateParameterSpace(
        preprocess(customParameterFactory().create("custom1")),
        matcher(
            hasParameters(1),
            parametersAreAllInstancesOf(CustomParameter.class),
            hasConstraints(0)
        ));
  }

  @Test
  public void givenSingleCustomParameterUnderOneConstraint$whenPreprocess$thenConvertedToSimpleParameter() {
    validateParameterSpace(
        preprocess(
            singletonList(customParameterFactory().create("custom1")),
            singletonList(Constraint.create(tuple -> true, "custom1"))
        ),
        matcher(
            ////
            // Non simple parameter involved in a constraint should be converted
            // into simple parameter
            hasParameters(1),
            parametersAreAllInstancesOf(Parameter.Simple.class),
            sizeOfParameterKnownValuesSatisfies(
                "custom1",
                name(">0",
                    (Integer size) -> size > 0
                )),
            allKnownValuesOfParameterSatisfy(
                "custom1",
                name(
                    "Instance of ValuePair",
                    t -> t instanceof CustomParameter.ValuePair
                )),
            hasConstraints(1)
        ));
  }

  @Test
  public void givenTwoCustomParameterUnderOneConstraintInvolvingOneParameter$whenPreprocess$thenInvolvedOneConvertedInToSimpleWhileTheOtherKept() {
    validateParameterSpace(
        preprocess(
            asList(
                customParameterFactory().create("custom1"),
                customParameterFactory().create("custom2")
            ),
            singletonList(Constraint.create((Tuple tuple) -> true, "custom1"))
        ),
        matcher(
            ////
            // Non simple parameter involved in a constraint should be converted
            // into simple parameter, while parameter(s) not involved in any constraints
            // should be passed through.
            hasParameters(2),
            parameterIsInstanceOf("custom1", Parameter.Simple.class),
            sizeOfParameterKnownValuesSatisfies(
                "custom1",
                name(">0",
                    (Integer size) -> size > 0
                )),
            allKnownValuesOfParameterSatisfy(
                "custom1",
                name(
                    "Instance of ValuePair",
                    t -> t instanceof CustomParameter.ValuePair
                )),
            parameterIsInstanceOf("custom2", CustomParameter.class),
            sizeOfParameterKnownValuesSatisfies(
                "custom2",
                name("==0",
                    (Integer size) -> size == 0
                )),
            hasConstraints(1)
        ));
  }

  @Test
  public void givenTwoCustomParametersUnderOneConstraintInvolvingBoth$whenPreprocess$thenBothConvertedIntoSimpleParameters() {
    validateParameterSpace(
        preprocess(
            asList(
                customParameterFactory().create("custom1"),
                customParameterFactory().create("custom2")
            ),
            singletonList(Constraint.create((Tuple tuple) -> true, "custom1", "custom2"))
        ),
        matcher(
            ////
            // Non simple parameter involved in a constraint should be converted
            // into simple parameter
            hasParameters(2),
            parameterIsInstanceOf("custom1", Parameter.Simple.class),
            sizeOfParameterKnownValuesSatisfies(
                "custom1",
                name(">0",
                    (Integer size) -> size > 0
                )),
            allKnownValuesOfParameterSatisfy(
                "custom1",
                name(
                    "Instance of ValuePair",
                    t -> t instanceof CustomParameter.ValuePair
                )),
            parameterIsInstanceOf("custom2", Parameter.Simple.class),
            sizeOfParameterKnownValuesSatisfies(
                "custom2",
                name(">0",
                    (Integer size) -> size > 0
                )),
            allKnownValuesOfParameterSatisfy(
                "custom2",
                name(
                    "Instance of ValuePair",
                    t -> t instanceof CustomParameter.ValuePair
                )),
            hasConstraints(1)
        ));
  }
}

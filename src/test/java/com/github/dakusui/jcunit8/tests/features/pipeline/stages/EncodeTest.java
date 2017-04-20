package com.github.dakusui.jcunit8.tests.features.pipeline.stages;

import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.tests.features.pipeline.testbase.PipelineTestBase;
import org.junit.Test;

import java.util.Objects;

import static com.github.dakusui.jcunit8.tests.features.pipeline.testbase.FactorSpaceUtils.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class EncodeTest extends PipelineTestBase {
  @Test
  public void givenOneFactor$whenEncode$thenFactorSpaceIsGenerated() {
    validateFactorSpace(
        encode(
            singletonList(
                simpleParameterFactory("A1", "A2").create("a")
            ),
            singletonList(
                Constraint.create(
                    tuple -> true,
                    "a"
                )
            )),
        matcher(
            sizeOfFactorsIs(name("==1", value -> value == 1)),
            factorSatisfies("a", factor -> Objects.equals(factor.getLevels(), asList("A1", "A2"))),
            sizeOfConstraintsIs(name("==1", value -> value == 1))
        )
    );
  }
}

package com.github.dakusui.jcunitx.tests.features.pipeline.stages;

import com.github.dakusui.jcunitx.factorspace.Constraint;
import com.github.dakusui.jcunitx.factorspace.Factor;
import com.github.dakusui.jcunitx.testutils.PipelineTestBase;
import com.github.dakusui.jcunitx.testutils.UTUtils;
import org.junit.Test;

import java.util.Objects;

import static com.github.dakusui.jcunitx.testutils.FactorSpaceUtils.*;
import static com.github.dakusui.jcunitx.testutils.UTUtils.matcher;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class EncodeTest extends PipelineTestBase {
  @Test
  public void givenOneFactorWithConstantConstraint$whenEncode$thenFactorSpaceIsGenerated() {
    validateFactorSpace(
        encode(
            singletonList(
                simpleParameterFactory("A1", "A2").create("a")
            ),
            singletonList(
                Constraint.create(
                    "alwaysTrue[a]",
                    tuple -> true,
                    "a"
                )
            )),
        matcher(
            sizeOfFactorsIs("==1", value -> value == 1),
            factorSatisfies(
                "a",
                "{x}.getLevels()==[A1,A2]",
                (Factor factor) -> Objects.equals(
                    factor.getLevels(),
                    asList("A1", "A2")
                )
            ),
            sizeOfConstraintsIs("==1", value -> value == 1)
        )
    );
  }

  @Test
  public void givenOneFactorWithNormalConstraint$whenEncode$thenFactorSpaceIsGenerated() {
    validateFactorSpace(
        encode(
            singletonList(
                simpleParameterFactory("A1", "A2", "A3").create("a")
            ),
            singletonList(
                Constraint.create(
                    "A3=a",
                    tuple -> Objects.equals(tuple.get("a"), "A3"),
                    "a"
                )
            )),
        matcher(
            sizeOfFactorsIs("==1", value -> value == 1),
            factorSatisfies(
                "a",
                "{x}.getLevels()==[A1,A2,A3]",
                (Factor factor) -> Objects.equals(
                    factor.getLevels(),
                    asList("A1", "A2", "A3")
                )
            ),
            sizeOfConstraintsIs("==1", value -> value == 1)
        )
    );
  }

  @Test
  public void givenOneFactorWithImpossibleConstraint$whenEncode$thenFactorSpaceIsGenerated() {
    validateFactorSpace(
        encode(
            singletonList(
                simpleParameterFactory("A1", "A2").create("a")
            ),
            singletonList(
                Constraint.create(
                    "alwaysTrue[a]",
                    tuple -> true,
                    "a"
                )
            )),
        UTUtils.matcherFromPredicates(
            sizeOfFactorsIs("==1", value -> value == 1),
            factorSatisfies(
                "a",
                "{x}.getLevels()==[A1, A2]",
                factor -> Objects.equals(factor.getLevels(), asList("A1", "A2"))
            ),
            sizeOfConstraintsIs("==1", value -> value == 1)
        )
    );
  }


}

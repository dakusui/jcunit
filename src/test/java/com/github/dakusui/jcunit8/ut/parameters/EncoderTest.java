package com.github.dakusui.jcunit8.ut.parameters;

import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import org.hamcrest.Matcher;
import org.junit.Test;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertThat;

public class EncoderTest extends PipelineTestBase {
  @Test
  public void whenEncodeOneFactor$thenFactorSpaceIsGenerated() {
    validate(
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
        matcher()
    );
  }

  private static void validate(FactorSpace factorSpace, Matcher<FactorSpace> matcher) {
    System.out.println("factors");
    factorSpace.getFactors().stream()
        .map(Object::toString)
        .map(s -> "  " + s)
        .forEach(System.out::println);
    System.out.println("constraints");
    factorSpace.getConstraints().stream()
        .map(Object::toString)
        .map(s -> "  " + s)
        .forEach(System.out::println);
    assertThat(factorSpace, matcher);
  }
}

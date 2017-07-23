package com.github.dakusui.jcunit8.testutils;

import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import org.hamcrest.Matcher;

import java.util.function.Predicate;

import static com.github.dakusui.jcunit8.testutils.UTUtils.oracle;
import static org.junit.Assert.assertThat;

public enum FactorSpaceUtils {
  ;

  public static TestOracle<FactorSpace, Integer> sizeOfFactorsIs(String description, Predicate<Integer> predicate) {
    return oracle(
        "{x}.getFactors().size()",
        factorSpace -> factorSpace.getFactors().size(),
        description,
        predicate
    );
  }

  public static TestOracle<FactorSpace, Integer> sizeOfConstraintsIs(String description, Predicate<Integer> predicate) {
    return new TestOracle.Builder<FactorSpace, Integer>()
        .withTransformer(
            "{x}.getConstraints().size()",
            factorSpace -> factorSpace.getConstraints().size()
        )
        .withTester(
            description,
            predicate
        )
        .build();
  }

  public static TestOracle<FactorSpace, Factor> factorSatisfies(String factorName, String predicateDescription, Predicate<Factor> predicate) {
    return new TestOracle.Builder<FactorSpace, Factor>()
        .withTransformer(
            String.format("{x}.getFactor(%s)", factorName),
            factorSpace -> factorSpace.getFactor(factorName)
        )
        .withTester(
            predicateDescription,
            predicate
        )
        .build();
  }

  public static void validateFactorSpace(FactorSpace factorSpace, Matcher<FactorSpace> matcher) {
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

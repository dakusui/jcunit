package com.github.dakusui.jcunit8.tests.features.pipeline.testbase;

import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.tests.features.UTBase;
import org.hamcrest.Matcher;

import java.util.function.Predicate;

import static org.junit.Assert.assertThat;

public enum FactorSpaceUtils {
  ;

  public static Predicate<FactorSpace> sizeOfFactorsIs(Predicate<Integer> predicate) {
    return UTBase.name(
        String.format("Size of factors should be '%s'", predicate),
        factorSpace -> predicate.test(factorSpace.getFactors().size())
    );
  }

  public static Predicate<FactorSpace> sizeOfConstraintsIs(Predicate<Integer> predicate) {
    return UTBase.name(
        String.format("Size of constraints should be '%s'", predicate),
        factorSpace -> predicate.test(factorSpace.getConstraints().size())
    );
  }

  public static Predicate<FactorSpace> factorSatisfies(String name, Predicate<Factor> predicate) {
    return UTBase.name(
        String.format("Factor '%s' should satisfy '%s'", name, predicate),
        factorSpace -> predicate.test(factorSpace.getFactor(name))
    );

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

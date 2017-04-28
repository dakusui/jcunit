package com.github.dakusui.jcunit8.testutils;

import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.factorspace.ParameterSpace;
import org.hamcrest.Matcher;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertThat;

public enum ParameterSpaceUtils {
  ;

  public static TestOracle<ParameterSpace, List<Object>> allKnownValuesOfParameterSatisfy(String parameterName, Predicate<Object> predicate) {
    return UTUtils.oracle(
        String.format("All values of parameter '%s'  should satisfy '%s'", parameterName, predicate),
        (ParameterSpace target) -> target.getParameter(parameterName).getKnownValues(),
        "",
        (List<Object> knownValues) -> knownValues.stream().allMatch(predicate)
    );
  }

  public static TestOracle<ParameterSpace, Integer> sizeOfParameterKnownValuesSatisfies(String parameterName, Predicate<Integer> predicate) {
    return UTUtils.oracle(
        String.format("Size of known values (%s) should be %s", parameterName, predicate),
        (ParameterSpace target) -> target.getParameter(parameterName).getKnownValues().size(),
        "",
        predicate::test
    );
  }

  public static Predicate<ParameterSpace> knownValuesOfParameterAre(String parameterName, Object... values) {
    return UTUtils.oracle(
        String.format("Values of parameter '%s'  are %s", parameterName, asList(values)),
        (ParameterSpace target) -> Objects.equals(
            target.getParameter(parameterName).getKnownValues(),
            asList(values)));
  }

  public static TestOracle<ParameterSpace, Integer> hasConstraints(int numConstraints) {
    return UTUtils.oracle(
        "",
        (ParameterSpace target) -> target.getConstraints().size(),
        String.format("Has %s constraint(s)", numConstraints),
        size -> size == numConstraints);
  }

  public static TestOracle<ParameterSpace, Integer> hasParameters(int numParameters) {
    return UTUtils.oracle(
        "Number of parameters ",
        (ParameterSpace target) -> target.getParameterNames().size(),
        String.format("==%s", numParameters),
        (Integer value) -> value == numParameters
    );
  }

  public static TestOracle<ParameterSpace, List<Class>> parametersAreAllInstancesOf(Class<? extends Parameter> parameterClass) {
    return UTUtils.oracle(
        String.format("Parameters are all %s", parameterClass.getSimpleName()),
        (ParameterSpace target) ->
            target.getParameterNames().stream()
                .map((String s) -> target.getParameter(s).getClass())
                .collect(toList()),
        "",
        (List<Class> parameterClasses) -> parameterClasses.stream()
            .allMatch(parameterClass::isAssignableFrom)
    );
  }

  public static TestOracle<ParameterSpace, Class> parameterIsInstanceOf(String parameterName, Class<? extends Parameter> parameterClass) {
    return UTUtils.oracle(
        "",
        (ParameterSpace parameterSpace) -> parameterSpace.getParameter(parameterName).getClass(),
        String.format("Parameter '%s' is instance of '%s'", parameterName, parameterClass.getSimpleName()),
        (Predicate<Class>) parameterClass::isAssignableFrom
    );
  }

  public static void validateParameterSpace(ParameterSpace parameterSpace, Matcher<ParameterSpace> matcher) {
    System.out.println("parameters");
    parameterSpace.getParameterNames()
        .forEach(
            parameterName -> System.out.println("  " + parameterSpace.getParameter(parameterName))
        );
    System.out.println("constraints");
    parameterSpace.getConstraints().forEach(
        o -> System.out.println("  " + o)
    );
    assertThat(parameterSpace, matcher);
  }
}

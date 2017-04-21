package com.github.dakusui.jcunit8.testutils;

import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.factorspace.ParameterSpace;
import org.hamcrest.Matcher;

import java.util.Objects;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertThat;

public enum ParameterSpaceUtils {
  ;

  public static Predicate<ParameterSpace> allKnownValuesOfParameterSatisfy(String parameterName, Predicate<Object> predicate) {
    return UTBase.name(
        String.format("All values of parameter '%s'  should satisfy '%s'", parameterName, predicate),
        (ParameterSpace target) ->
            target.getParameter(parameterName).getKnownValues().stream()
                .allMatch(predicate)
    );
  }

  public static Predicate<ParameterSpace> sizeOfParameterKnownValuesSatisfies(String parameterName, Predicate<Integer> predicate) {
    return UTBase.name(
        String.format("Size of known values (%s) should be %s", parameterName, predicate),
        (ParameterSpace target) ->
            predicate.test(target.getParameter(parameterName).getKnownValues().size())
    );
  }

  public static Predicate<ParameterSpace> knownValuesOfParameterAre(String parameterName, Object... values) {
    return UTBase.name(
        String.format("Values of parameter '%s'  are %s", parameterName, asList(values)),
        (ParameterSpace target) -> Objects.equals(
            target.getParameter(parameterName).getKnownValues(),
            asList(values)));
  }

  public static Predicate<ParameterSpace> hasConstraints(int numConstraints) {
    return UTBase.name(
        String.format("Has %s constraint(s)", numConstraints),
        (ParameterSpace target) -> target.getConstraints().size() == numConstraints);
  }

  public static Predicate<ParameterSpace> hasParameters(int numParameters) {
    return UTBase.name(String.format("Has %s parameter(s)", numParameters),
        (ParameterSpace target) -> target.getParameterNames().size() == numParameters);
  }

  public static Predicate<ParameterSpace> parametersAreAllInstancesOf(Class<? extends Parameter> parameterClass) {
    return UTBase.name(String.format("Parameters are all %s", parameterClass.getSimpleName()),
        (ParameterSpace target) -> target.getParameterNames().stream()
            .map(target::getParameter)
            .allMatch(o -> parameterClass.isAssignableFrom(o.getClass()))
    );
  }

  public static Predicate<ParameterSpace> parameterIsInstanceOf(String parameterName, Class<? extends Parameter> parameterClass) {
    return UTBase.name(
        String.format("Parameter '%s' is instance of '%s'", parameterName, parameterClass.getSimpleName()),
        parameterSpace -> parameterClass.isAssignableFrom(parameterSpace.getParameter(parameterName).getClass())
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

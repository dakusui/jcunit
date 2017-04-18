package com.github.dakusui.jcunit8.ut.parameters;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.factorspace.ParameterSpace;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.Objects;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertThat;

public class PreprocessTest extends PipelineTestBase {
  @Test
  public void whenPreprocessSimpleParameter$thenCorrectParameterSpaceIsPrepared() {
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
  public void whenPreprocessTwoSimpleParameters$thenCorrectParameterSpaceIsPrepared() {
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
  public void whenPreprocessSingleSimpleParameterUnderOneConstraint$thenCorrectParameterSpaceIsPrepared() {
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
  public void whenPreprocessTwoSimpleParameterUnderOneConstraint$thenCorrectParameterSpaceIsPrepared() {
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
  public void whenPreprocessCustomParameter$thenDone() {
    validateParameterSpace(
        preprocess(customParameterFactory().create("custom1")),
        matcher(
            hasParameters(1),
            parametersAreAllInstancesOf(CustomParameter.class),
            hasConstraints(0)
        ));
  }

  @Test
  public void whenPreprocessSingleCustomParameterUnderOneConstraint$thenCorrectParameterSpaceIsPrepared() {
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
                tag(">0",
                    (Integer size) -> size > 0
                )),
            allKnownValuesOfParameterSatisfy(
                "custom1",
                tag(
                    "Instance of ValuePair",
                    t -> t instanceof CustomParameter.ValuePair
                )),
            hasConstraints(1)
        ));
  }

  @Test
  public void whenPreprocessTwoCustomParameterUnderOneConstraintInvolvingOneParameter$thenCorrectParameterSpaceIsPrepared() {
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
                tag(">0",
                    (Integer size) -> size > 0
                )),
            allKnownValuesOfParameterSatisfy(
                "custom1",
                tag(
                    "Instance of ValuePair",
                    t -> t instanceof CustomParameter.ValuePair
                )),
            parameterIsInstanceOf("custom2", CustomParameter.class),
            sizeOfParameterKnownValuesSatisfies(
                "custom2",
                tag("==0",
                    (Integer size) -> size == 0
                )),
            hasConstraints(1)
        ));
  }

  @Test
  public void whenPreprocessTwoCustomParameterUnderOneConstraintInvolvingBoth$thenDone() {
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
                tag(">0",
                    (Integer size) -> size > 0
                )),
            allKnownValuesOfParameterSatisfy(
                "custom1",
                tag(
                    "Instance of ValuePair",
                    t -> t instanceof CustomParameter.ValuePair
                )),
            parameterIsInstanceOf("custom2", Parameter.Simple.class),
            sizeOfParameterKnownValuesSatisfies(
                "custom2",
                tag(">0",
                    (Integer size) -> size > 0
                )),
            allKnownValuesOfParameterSatisfy(
                "custom2",
                tag(
                    "Instance of ValuePair",
                    t -> t instanceof CustomParameter.ValuePair
                )),
            hasConstraints(1)
        ));
  }

  private static void validateParameterSpace(ParameterSpace parameterSpace, Matcher<ParameterSpace> matcher) {
    System.out.println("parameters");
    parameterSpace.getParameterNames().forEach(
        parameterName -> System.out.println("  " + parameterSpace.getParameter(parameterName))
    );
    System.out.println("constraints");
    parameterSpace.getConstraints().forEach(
        o -> System.out.println("  " + o)
    );
    assertThat(parameterSpace, matcher);
  }

  private static Predicate<ParameterSpace> allKnownValuesOfParameterSatisfy(String parameterName, Predicate<Object> predicate) {
    return tag(
        String.format("All values of parameter '%s'  should satisfy '%s'", parameterName, predicate),
        (ParameterSpace target) ->
            target.getParameter(parameterName).getKnownValues().stream()
                .allMatch(predicate)
    );
  }

  private static Predicate<ParameterSpace> sizeOfParameterKnownValuesSatisfies(String parameterName, Predicate<Integer> predicate) {
    return tag(
        String.format("Size of known values (%s) should be %s", parameterName, predicate),
        (ParameterSpace target) ->
            predicate.test(target.getParameter(parameterName).getKnownValues().size())
    );
  }

  private static Predicate<ParameterSpace> knownValuesOfParameterAre(String parameterName, Object... values) {
    return tag(
        String.format("Values of parameter '%s'  are %s", parameterName, asList(values)),
        (ParameterSpace target) -> Objects.equals(
            target.getParameter(parameterName).getKnownValues(),
            asList(values)));
  }

  private static Predicate<ParameterSpace> hasConstraints(int numConstraints) {
    return tag(
        String.format("Has %s constraint(s)", numConstraints),
        (ParameterSpace target) -> target.getConstraints().size() == numConstraints);
  }

  private static Predicate<ParameterSpace> hasParameters(int numParameters) {
    return tag(String.format("Has %s parameter(s)", numParameters),
        (ParameterSpace target) -> target.getParameterNames().size() == numParameters);
  }

  private static Predicate<ParameterSpace> parametersAreAllInstancesOf(Class<? extends Parameter> parameterClass) {
    return tag(String.format("Parameters are all %s", parameterClass.getSimpleName()),
        (ParameterSpace target) -> target.getParameterNames().stream()
            .map(target::getParameter)
            .allMatch(o -> parameterClass.isAssignableFrom(o.getClass()))
    );
  }

  private static Predicate<ParameterSpace> parameterIsInstanceOf(String parameterName, Class<? extends Parameter> parameterClass) {
    return tag(
        String.format("Parameter '%s' is instance of '%s'", parameterName, parameterClass.getSimpleName()),
        parameterSpace -> parameterClass.isAssignableFrom(parameterSpace.getParameter(parameterName).getClass())
    );
  }
}

package com.github.dakusui.jcunit8.ut.parameters;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.factorspace.ParameterSpace;
import com.github.dakusui.jcunit8.pipeline.Config;
import com.github.dakusui.jcunit8.pipeline.Pipeline;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testsuite.TestSuite;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

abstract class PipelineTestBase {

  TestSuite<Tuple> generateTestSuite(Parameter... parameters) {
    ParameterSpace parameterSpace = new ParameterSpace.Builder()
        .addAllParameters(asList(parameters))
        .build();
    return new Pipeline.Standard<Tuple>().generateTestSuite(buildConfig(), parameterSpace);
  }

  TestSuite<Tuple> generateTestSuite(List<Parameter> parameters, List<Constraint> constraints) {
    ParameterSpace parameterSpace = new ParameterSpace.Builder()
        .addAllParameters(parameters)
        .addAllConstraints(constraints)
        .build();
    return new Pipeline.Standard<Tuple>().generateTestSuite(buildConfig(), parameterSpace);
  }

  ParameterSpace preprocess(Parameter... parameters) {
    return new Pipeline.Standard<Tuple>().preprocess(buildConfig(), new ParameterSpace.Builder().addAllParameters(asList(parameters)).build());
  }

  ParameterSpace preprocess(List<Parameter> parameters, List<Constraint> constraints) {
    return new Pipeline.Standard<Tuple>().preprocess(buildConfig(), new ParameterSpace.Builder().addAllParameters(parameters).addAllConstraints(constraints).build());
  }

  SchemafulTupleSet engine(List<Parameter> parameters, List<Constraint> constraints) {
    return new Pipeline.Standard<Tuple>().engine(buildConfig(), new ParameterSpace.Builder().addAllParameters(parameters).addAllConstraints(constraints).build());
  }

  FactorSpace encode(List<Parameter> parameters, List<Constraint> constraints) {
    return buildConfig()
        .encoder().apply(
            new ParameterSpace.Builder()
                .addAllParameters(parameters)
                .addAllConstraints(constraints)
                .build()
        );
  }

  Parameter.Factory<CustomParameter.ValuePair> customParameterFactory() {
    return new Parameter.Factory.Base<CustomParameter.ValuePair>() {
      @Override
      public Parameter<CustomParameter.ValuePair> create(String name) {
        return new CustomParameter(name, asList("hello", "world", "everyone"));
      }
    };
  }

  Parameter.Factory<String> simpleParameterFactory(String... values) {
    return Parameter.Simple.Factory.of(asList(values));
  }

  Parameter.Factory<String> simpleParameterFactoryWithDefaultValues() {
    return simpleParameterFactory("default", "values");
  }

  static <T> Predicate<T> tag(String tag, Predicate<T> predicate) {
    return new Predicate<T>() {
      @Override
      public boolean test(T t) {
        return predicate.test(t);
      }

      @Override
      public String toString() {
        return tag;
      }
    };
  }

  @SafeVarargs
  static <T> Matcher<T> matcher(Predicate<T>... predicates) {
    return matcher(asList(predicates));
  }

  private static <T> Matcher<T> matcher(List<Predicate<T>> predicates) {
    return new BaseMatcher<T>() {
      @Override
      public boolean matches(Object o) {
        //noinspection unchecked
        T target = (T) o;
        for (Predicate<T> each : predicates) {
          if (!each.test(target))
            return false;
        }
        return true;
      }

      public void describeMismatch(Object item, Description description) {
        description.appendText("was ").appendValue(item).appendText(" ");
        description.appendText("that does not satisfy ");
        //noinspection unchecked
        description.appendText(
            predicates.stream()
                .filter((Predicate<T> target) -> !target.test((T) item))
                .map(Predicate::toString)
                .collect(Collectors.joining(", ", "[", "]"))
        );
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(
            predicates.stream()
                .map(Predicate::toString)
                .collect(Collectors.joining(", "))
        );
      }
    };
  }

  private Config<Tuple> buildConfig() {
    return Config.Builder.forTuple(
        new Requirement.Builder()
            .withNegativeTestGeneration(false)
            .build())
        .build();
  }


}

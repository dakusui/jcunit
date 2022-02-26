package com.github.dakusui.jcunitx.metamodel;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.factorspace.Constraint;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

/**
 * A test input parameter space.
 * It consists of parameters and constraints over them.
 *
 * This interface defines the user-facing model of the system under test.
 */
public interface ParameterSpace {
  /**
   * Encodes the given {@code seeds} into its internal representation.
   * **NOTE:** that the {@code seeds} need to be complete, that means all the parameters in a seed must have a concrete value.
   * This is a limitation of the current version of JCUnit, because in the CIT area "seeding" is done for the
   *
   * @param parameterSpace The parameter space in which the {@code seeds} are defined.
   * @param seeds          A set of test cases from which the final test suite is generated.
   * @return A list of encoded seeds.
   */
  static List<AArray> encodeSeeds(ParameterSpace parameterSpace, List<AArray> seeds) {
    return seeds.stream()
        .map(parameterSpace::encodeSeed)
        .collect(toList());
  }

  /**
   * Returns the list of parameter names.
   *
   * @return The list of parameter names.
   */
  List<String> getParameterNames();

  /**
   * Returns a parameter specified by the {@code name}.
   *
   * @param name The name of the parameter
   * @param <P>  The type of the parameter values.
   * @return A parameter specified by {@code name}
   */
  <P> Parameter<P> getParameter(String name);

  /**
   * Returns a list of constraints defined over the parameters in this space.
   *
   * @return The list of constraints.
   */
  List<Constraint> getConstraints();

  /**
   * This corresponds to the "Encode" stage in the "Engine" pipeline.
   *
   * @param seed A seed
   * @return An encoded seed
   */
  default AArray encodeSeed(AArray seed) {
    AArray.Builder builder = AArray.builder();
    getParameterNames()
        .forEach(each -> getParameter(each).decomposeValue(seed.get(each)).ifPresent(builder::putAll));
    return builder.build();
  }

  class Builder {
    List<Parameter<?>> parameters  = new LinkedList<>();
    List<Constraint>   constraints = new LinkedList<>();

    public Builder addParameter(Parameter<?> parameter) {
      this.parameters.add(parameter);
      return this;
    }

    public Builder addAllParameters(Collection<? extends Parameter<?>> parameters) {
      parameters.forEach(Builder.this::addParameter);
      return this;
    }

    public Builder addConstraint(Constraint constraint) {
      this.constraints.add(constraint);
      return this;
    }

    public Builder addAllConstraints(Collection<? extends Constraint> constraints) {
      constraints.forEach(Builder.this::addConstraint);
      return this;
    }

    public ParameterSpace build() {
      return new ParameterSpace() {
        @Override
        public List<String> getParameterNames() {
          return parameters.stream().map(Parameter::getName).collect(toList());
        }

        @SuppressWarnings("unchecked")
        @Override
        public <P> Parameter<P> getParameter(String name) {
          return (Parameter<P>) (parameters.stream()
              .filter(parameter -> parameter.getName().equals(name))
              .findFirst()
              .orElseThrow(() -> new RuntimeException(undefinedParameterMessage(name))));
        }

        private String undefinedParameterMessage(String name) {
          return format(
              "Parameter '%s' was requested but not found. Existing parameters are %s",
              name,
              getParameterNames()
          );
        }

        @Override
        public List<Constraint> getConstraints() {
          return unmodifiableList(constraints);
        }

        @Override
        public String toString() {
          return format("parameters:%s,constraints:%s", parameters, constraints);
        }
      };
    }
  }
}

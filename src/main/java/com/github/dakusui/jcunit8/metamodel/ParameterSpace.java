package com.github.dakusui.jcunit8.metamodel;

import com.github.dakusui.jcunit.core.tuples.Aarray;
import com.github.dakusui.jcunit8.factorspace.Constraint;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

/**
 * A test input parameter space.
 * It consists of parameters and constraints over them.
 */
public interface ParameterSpace {
  static List<Aarray> encodeSeedTuples(ParameterSpace parameterSpace, List<Aarray> seeds) {
    return seeds.stream(
    ).map(
        parameterSpace::encodeTuple
    ).collect(
        toList()
    );
  }

  List<String> getParameterNames();

  <P> Parameter<P> getParameter(String name);

  List<Constraint> getConstraints();

  /**
   * This corresponds to the "Encode" stage in the "Engine" pipeline.
   *
   * @param tuple An input key-value pairs
   * @return An encoded tuple
   */
  default Aarray encodeTuple(Aarray tuple) {
    Aarray.Builder builder = Aarray.builder();
    getParameterNames()
        .forEach(each -> getParameter(each).decomposeValue(tuple.get(each)).ifPresent(builder::putAll));
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
          return (Parameter<P>) (parameters.stream(

          ).filter(
              parameter -> parameter.getName().equals(name)
          ).findFirst(
          ).orElseThrow(
              () -> new RuntimeException(format(
                  "Parameter '%s' was requested but not found. Existing parameters are %s",
                  name,
                  getParameterNames()
              ))
          ));
        }

        @Override
        public List<Constraint> getConstraints() {
          return Collections.unmodifiableList(constraints);
        }

        @Override
        public String toString() {
          return format("parameters:%s,constraints:%s", parameters, constraints);
        }
      };
    }
  }
}

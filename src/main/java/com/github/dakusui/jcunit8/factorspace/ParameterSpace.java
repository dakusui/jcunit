package com.github.dakusui.jcunit8.factorspace;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public interface ParameterSpace {
  List<String> getParameterNames();

  <P> Parameter<P> getParameter(String name);

  List<Constraint> getConstraints();

  class Builder {
    List<Parameter>  parameters  = new LinkedList<>();
    List<Constraint> constraints = new LinkedList<>();

    public Builder addParameter(Parameter parameter) {
      this.parameters.add(parameter);
      return this;
    }

    public Builder addAllParameters(Collection<? extends Parameter> parameters) {
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
          return parameters.stream().map(Parameter::getName).collect(Collectors.toList());
        }

        @Override
        public <P> Parameter<P> getParameter(String name) {
          //noinspection unchecked,OptionalGetWithoutIsPresent
          return (Parameter<P>) (parameters
              .stream()
              .filter(parameter -> parameter.getName().equals(name))
              .findFirst().<Parameter<P>>get());
        }

        @Override
        public List<Constraint> getConstraints() {
          return Collections.unmodifiableList(constraints);
        }

        @Override
        public String toString() {
          return String.format("parameters:%s,constraints:%s", parameters, constraints);
        }
      };
    }
  }
}

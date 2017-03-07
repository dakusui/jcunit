package com.github.dakusui.jcunit8.model.parameterspace;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.model.factorspace.Constraint;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface ParameterSpace<T> {
  List<String> getParameterNames();

  <P> Parameter<P> getParameter(String name);

  List<Constraint<T>> getConstraints();

  T createObject(Tuple tuple);

  class Builder<T> {
    List<Parameter>     parameters  = new LinkedList<>();
    List<Constraint<T>> constraints = new LinkedList<>();
    private Function<Tuple, T> objectFactory;

    public Builder addParameter(Parameter parameter) {
      this.parameters.add(parameter);
      return this;
    }

    public Builder addConstraint(Constraint<T> constraint) {
      this.constraints.add(constraint);
      return this;
    }

    public Builder setObjectFactory(Function<Tuple, T> objectFactory) {
      this.objectFactory = objectFactory;
      return this;
    }

    public ParameterSpace<T> build() {
      return new ParameterSpace<T>() {
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
        public List<Constraint<T>> getConstraints() {
          return Collections.unmodifiableList(constraints);
        }

        @Override
        public T createObject(Tuple tuple) {
          return objectFactory.apply(tuple);
        }
      };
    }
  }
}

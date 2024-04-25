package com.github.dakusui.jcunit8.pipeline.stages;

import com.github.dakusui.jcunit8.exceptions.FrameworkException;
import com.github.dakusui.jcunit8.factorspace.*;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public interface Encoder extends Function<ParameterSpace, FactorSpace> {
  class Standard implements Encoder {
    /**
     * {@code parameterSpace} must not contain any non-simple parameter that are
     * involved in constraints.
     *
     * @param parameterSpace preprocessed parameter space from which factor space is generated.
     */
    @Override
    public FactorSpace apply(ParameterSpace parameterSpace) {
      streamParameters(parameterSpace).forEach(
          parameter -> FrameworkException.check(parameter, eachParameter -> eachParameter instanceof Parameter.Simple ||
              parameterSpace.getConstraints().stream()
                  .noneMatch(constraint -> constraint.involvedKeys().contains(eachParameter.getName())))
      );
      List<FactorSpace> factorSpaces = streamParameters(parameterSpace)
          .map(Parameter::toFactorSpace)
          .collect(toList());
      List<Factor> factors = factorSpaces.stream()
          .flatMap(factorSpace -> factorSpace.getFactors().stream())
          .collect(toList());
      List<Constraint> constraints = Stream.concat(
          parameterSpace.getConstraints().stream(),
          factorSpaces.stream()
              .flatMap(factorSpace -> factorSpace.getConstraints().stream())
      ).collect(toList());
      return FactorSpace.create(
          factors,
          constraints
      );
    }

    private Stream<Parameter> streamParameters(ParameterSpace parameterSpace) {
      return parameterSpace.getParameterNames().stream()
          .map(parameterSpace::getParameter);
    }
  }
}

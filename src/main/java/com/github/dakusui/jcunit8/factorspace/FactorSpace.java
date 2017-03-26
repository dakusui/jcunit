package com.github.dakusui.jcunit8.factorspace;

import java.util.List;
import java.util.OptionalDouble;

public interface FactorSpace {
  static FactorSpace create(List<? extends Factor> factors, List<Constraint> constraints) {
    return new FactorSpace() {
      @Override
      public List<Factor> getFactors() {
        //noinspection unchecked
        return (List<Factor>) factors;
      }

      @Override
      public Characteristics getCharacteristics() {
        return new Characteristics.Impl(factors, constraints);
      }

      @Override
      public List<Constraint> getConstraints() {
        return constraints;
      }
    };
  }

  List<Constraint> getConstraints();

  List<Factor> getFactors();

  Characteristics getCharacteristics();

  /*
   * IPO has a better performance than AETG:
   * IPO has a time complexity of O(v^3N^2log(N)) and
   * AETG has a time complexity of O(v^4N^2log(N)), where N is the number of
   * parameters, each of which has v values [1];
   */
  interface Characteristics {
    int numberOfConstraints();

    int numberOfFactors();

    double averageNumberOfLevels();

    class Impl implements Characteristics {
      private final List<Constraint>      constraints;
      private final List<Factor> factors;

      public Impl(List<? extends Factor> factors, List<Constraint> constraints) {
        //noinspection unchecked
        this.factors = (List<Factor>) factors;
        this.constraints = constraints;
      }

      @Override
      public int numberOfConstraints() {
        return constraints.size();
      }

      @Override
      public int numberOfFactors() {
        return factors.size();
      }

      @Override
      public double averageNumberOfLevels() {
        OptionalDouble value;
        return (value = factors.stream()
            .map(Factor::getLevels)
            .mapToInt(List::size).average()
        ).isPresent() ?
            value.getAsDouble() :
            Double.NaN;
      }

    }
  }
}

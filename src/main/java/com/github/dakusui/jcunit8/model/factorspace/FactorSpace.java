package com.github.dakusui.jcunit8.model.factorspace;

import java.util.List;

public interface FactorSpace {
  List<Factor> getFactors();

  List<Constraint.ForTuple> getConstraints();

  Characteristics getCharacteristics();

  static FactorSpace merge(List<FactorSpace> factorSpaces) {
    // TODO
    return null;
  }

  /*
   * IPO has a better performance than AETG:
   * IPO has a time complexity of O(v^3N^2log(N)) and
   * AETG has a time complexity of O(v^4N^2log(N)), where N is the number of
   * parameters, each of which has v values [1];
   */
  interface Characteristics {
    int numberOfFactors();

    double averageNumberOfLevels();
  }

  static FactorSpace create(List<Factor> factors, List<Constraint.ForTuple> constraints) {
    return new FactorSpace() {
      @Override
      public List<Factor> getFactors() {
        return factors;
      }

      @Override
      public List<Constraint.ForTuple> getConstraints() {
        return constraints;
      }

      @Override
      public Characteristics getCharacteristics() {
        return null;
      }
    };
  }
}

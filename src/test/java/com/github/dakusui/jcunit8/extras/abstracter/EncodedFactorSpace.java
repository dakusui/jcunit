package com.github.dakusui.jcunit8.extras.abstracter;

import com.github.dakusui.jcunit8.extras.normalizer.compat.FactorSpaceSpecForExperiments;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public abstract class EncodedFactorSpace implements FactorSpace {

  abstract Function<String, String> factorNameEncoder();

  abstract Function<String, String> factorNameDecoder();

  abstract BiFunction<String, Object, Integer> valueEncoder();

  abstract BiFunction<String, Integer, Object> valueDecoder();

  public static class Builder {
    private final FactorSpace baseFactorSpace;

    public Builder(FactorSpace factorSpace) {
      this.baseFactorSpace = requireNonNull(factorSpace);
    }

    public EncodedFactorSpace build() {
      FactorSpaceSpecForExperiments factorSpaceSpec = new FactorSpaceSpecForExperiments();
      FactorSpace inner = factorSpaceSpec.build();
      return new EncodedFactorSpace() {
        @Override
        public List<Constraint> getConstraints() {
          return inner.getConstraints();
        }

        @Override
        public List<Factor> getFactors() {
          return inner.getFactors();
        }

        @Override
        public Factor getFactor(String name) {
          return inner.getFactor(name);
        }

        @Override
        Function<String, String> factorNameEncoder() {
          return null;
        }

        @Override
        Function<String, String> factorNameDecoder() {
          return null;
        }

        @Override
        BiFunction<String, Object, Integer> valueEncoder() {
          return null;
        }

        @Override
        BiFunction<String, Integer, Object> valueDecoder() {
          return null;
        }
      };
    }
  }
}

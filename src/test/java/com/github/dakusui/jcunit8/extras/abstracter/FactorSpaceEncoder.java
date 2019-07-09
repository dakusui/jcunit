package com.github.dakusui.jcunit8.extras.abstracter;

import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.dakusui.jcunit.core.utils.Checks.checkcond;

public class FactorSpaceEncoder {
  static class Locator {
    final int numLevels;
    final int index;

    Locator(int numLevels, int index) {
      this.numLevels = numLevels;
      this.index = index;
    }
  }

  private final FactorSpaceSpec            spec               = new FactorSpaceSpec();
  private final Map<Integer, List<Factor>> factors            = new HashMap<>();
  private final Map<String, Integer>       indicesInSameLevel = new HashMap<>();

  public FactorSpaceEncoder addFactor(Factor factor) {
    int factorSize = factor.getLevels().size();
    this.factors.putIfAbsent(factorSize, new ArrayList<>());
    this.indicesInSameLevel.put(factor.getName(), factors.get(factorSize).size());
    this.factors.get(factorSize).add(factor);
    this.spec.addFactor(factorSize);
    return this;
  }

  public FactorSpaceEncoder addConstraint(Constraint constraint) {
    return this;
  }

  public EncodedFactorSpace encoded() {
    return new EncodedFactorSpace() {
      final FactorSpace inner = spec.build();

      @Override
      Function<String, String> factorNameEncoder() {
        return null;
      }

      @Override
      Function<String, String> factorNameDecoder() {
        return encodedFactorName -> factors.get(inner.getFactor(encodedFactorName).getLevels().size())
            .get(factorIndexInSameNumLevelsOf(encodedFactorName)).getName();
      }

      private int factorIndexOf(String encodedFactorName) {
        checkcond(encodedFactorName.startsWith("p"));
        return Integer.valueOf(encodedFactorName.substring(1));
      }

      private int factorIndexInSameNumLevelsOf(String encodedFactorName) {
        return indicesInSameLevel.get(encodedFactorName);
      }

      @Override
      BiFunction<String, Object, Integer> valueEncoder() {
        return null;
      }

      @Override
      BiFunction<String, Integer, Object> valueDecoder() {
        return null;
      }

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
    };
  }

}

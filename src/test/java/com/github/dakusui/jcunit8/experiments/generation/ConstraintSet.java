package com.github.dakusui.jcunit8.experiments.generation;

import com.github.dakusui.jcunit8.extras.generators.ActsUtils;
import com.github.dakusui.jcunit8.extras.normalizer.compat.NormalizedConstraint;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public enum ConstraintSet {
  NONE {
    @Override
    public Optional<NormalizedConstraintFactory> constraintFactory(int offset) {
      return Optional.empty();
    }
  },
  BASIC {
    @Override
    public Optional<NormalizedConstraintFactory> constraintFactory(int offset) {
      return Optional.of(factorNames -> ActsUtils.createBasicConstraint(offset).apply(factorNames));
    }
  },
  BASIC_PLUS {
    @Override
    public Optional<NormalizedConstraintFactory> constraintFactory(int offset) {
      throw new UnsupportedOperationException();
    }
  };

  public abstract Optional<NormalizedConstraintFactory> constraintFactory(int offset);

  public interface NormalizedConstraintFactory extends Function<List<String>, NormalizedConstraint> {
  }
}

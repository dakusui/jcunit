package com.github.dakusui.jcunit8.experiments.generation;

import com.github.dakusui.jcunit8.experiments.peerj.acts.ActsUtils;
import com.github.dakusui.jcunit8.experiments.peerj.NormalizedConstraint;

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
  /*
    pi,1>pi,2∨pi,3>pi,4∨pi,5>pi,6∨pi,7>pi,8∨pi,9>pi,2(0≤i<n)
   */
  BASIC {
    @Override
    public Optional<NormalizedConstraintFactory> constraintFactory(int offset) {
      return Optional.of(factorNames -> ActsUtils.createBasicConstraint(offset).apply(factorNames));
    }
  },
  /*
    (pi,1>pi,2∨pi,3>pi,4∨pi,5>pi,6∨pi,7>pi,8∨pi,9>pi,2)∧pi,10>pi,1∧pi,9>pi,2∧pi,8>pi,3∧pi,7>pi,4∧pi,6>pi,5(0≤i<n)
   */
  BASIC_PLUS {
    @Override
    public Optional<NormalizedConstraintFactory> constraintFactory(int offset) {
      return Optional.of(factorNames -> ActsUtils.createBasicPlusConstraint(offset).apply(factorNames));
    }
  };

  public abstract Optional<NormalizedConstraintFactory> constraintFactory(int offset);

  public interface NormalizedConstraintFactory extends Function<List<String>, NormalizedConstraint> {
  }
}

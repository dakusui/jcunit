package com.github.dakusui.jcunit8.extras.normalizer;

import com.github.dakusui.jcunit8.factorspace.Factor;

import java.util.function.Function;

public interface NormalizedFactor extends Factor {
  Function<Integer, Integer> denormalizer();

  Object getRawLevel(int i);
}

package com.github.dakusui.jcunit8.extras.normalizer;

import com.github.dakusui.jcunit8.factorspace.Factor;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static com.github.dakusui.jcunit8.extras.normalizer.NormalizedFactor.Utils.Internal.validateValues;
import static java.util.stream.Collectors.toList;

public interface NormalizedFactor extends Factor {
  Function<Integer, Integer> denormalizer();

  Object getRawLevelFor(int i);

  enum Utils {
    ;

    static <T extends Comparable> NormalizedFactor normalizeNumberFactor(String normalizedName, Factor rawFactor, Class<T> klazz) {
      validateValues(rawFactor.getLevels(), v -> klazz.isAssignableFrom(v.getClass()));
      return new NormalizedFactor() {
        private List<Object> encodedValues = IntStream.range(0, rawFactor.getLevels().size())
            .boxed()
            .collect(toList());
        private final int[] indicesInRawFactor = new int[] {
        };

        @Override
        public Function<Integer, Integer> denormalizer() {
          return i -> indicesInRawFactor[i];
        }

        @Override
        public String getName() {
          return normalizedName;
        }

        @Override
        public Object getRawLevelFor(int indexInRawFactor) {
          return null;
        }


        @Override
        public List<Object> getLevels() {
          return null;
        }
      };
    }


    enum Internal {
      ;

      static void validateValues(Iterable<Object> values, Predicate<Object> condition) {
        for (Object each : values)
          if (!condition.test(each))
            throw new IllegalArgumentException(String.format("Invalid value:<%s> was found.", each));
      }
    }
  }
}

package com.github.dakusui.jcunit8.extras.normalizer;

import com.github.dakusui.jcunit8.factorspace.Factor;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public interface FactorNormalizer {
  NormalizedFactor normalize(Factor factor);

  static <T extends Comparable> FactorNormalizer createNumberFactorFrom(Class<T> klazz, Factor factor, String internalName) {
    Utils.validateValues(factor.getLevels(), v -> klazz.isAssignableFrom(v.getClass()));
    return (Factor f) -> new NormalizedFactor() {
      @Override
      public Function<Integer, Integer> denormalizer() {
        return i -> factor.getLevels().indexOf(sortedValues.get(i));
      }

      private List<Object> encodedValues = IntStream.range(0, factor.getLevels().size())
          .boxed()
          .collect(toList());
      @SuppressWarnings("unchecked")
      List<Object> sortedValues = factor.getLevels().stream()
          .map(e -> (T) e)
          .sorted(Comparable<T>::compareTo)
          .collect(toList());

      @Override
      public String getName() {
        return internalName;
      }

      @Override
      public List<Object> getLevels() {
        return encodedValues;
      }

      @Override
      public Object getRawLevel(int i) {
        return sortedValues.get(denormalizer().apply(i));
      }
    };
  }

  static void main(String... args) {
    System.out.println(Stream.of(1, 3, 2, 5, 4, 7).sorted(Integer::compareTo).collect(Collectors.toList()));
  }

  enum Utils {
    ;

    static void validateValues(Iterable<Object> values, Predicate<Object> condition) {
      for (Object each : values)
        if (!condition.test(each))
          throw new IllegalArgumentException(String.format("Invalid value:<%s> was found.", each));
    }
  }
}

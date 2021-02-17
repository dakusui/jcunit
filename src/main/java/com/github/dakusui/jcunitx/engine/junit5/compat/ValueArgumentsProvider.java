package com.github.dakusui.jcunitx.engine.junit5.compat;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.Preconditions;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class ValueArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<ValueSource> {
  private Object[] arguments;

  @Override
  public void accept(ValueSource source) {
    // @formatter:off
    List<Object> arrays =
        // Declaration of <Object> is necessary due to a bug in Eclipse Photon.
        Stream.<Object> of(
            source.shorts(),
            source.bytes(),
            source.ints(),
            source.longs(),
            source.floats(),
            source.doubles(),
            source.chars(),
            source.booleans(),
            source.strings(),
            source.classes()
        )
            .filter(array -> Array.getLength(array) > 0)
            .collect(toList());
    // @formatter:on

    Preconditions.condition(arrays.size() == 1, () -> "Exactly one type of input must be provided in the @"
        + ValueSource.class.getSimpleName() + " annotation, but there were " + arrays.size());

    Object originalArray = arrays.get(0);
    arguments = IntStream.range(0, Array.getLength(originalArray)) //
        .mapToObj(index -> Array.get(originalArray, index)) //
        .toArray();
  }

  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
    return Arrays.stream(arguments).map(Arguments::of);
  }
}

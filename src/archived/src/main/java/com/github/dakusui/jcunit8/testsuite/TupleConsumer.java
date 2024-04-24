package com.github.dakusui.jcunit8.testsuite;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.Checks;

import java.io.IOException;
import java.util.Formattable;
import java.util.Formatter;
import java.util.function.Consumer;

public interface TupleConsumer extends Consumer<Tuple>, Formattable {
  String getName();

  @Override
  default void formatTo(Formatter formatter, int flags, int width, int precision) {
    try {
      formatter.out().append(getName());
    } catch (IOException e) {
      throw Checks.wrap(e);
    }
  }
}

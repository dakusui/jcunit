package com.github.dakusui.jcunitx.testsuite;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.utils.Checks;

import java.io.IOException;
import java.util.Formattable;
import java.util.Formatter;
import java.util.function.Consumer;

public interface TestInputConsumer extends Consumer<AArray>, Formattable {
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

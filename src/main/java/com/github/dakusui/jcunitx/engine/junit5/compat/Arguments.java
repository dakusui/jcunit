package com.github.dakusui.jcunitx.engine.junit5.compat;

import org.junit.platform.commons.util.Preconditions;

public interface Arguments {
  Object[] get();
  static Arguments of(Object... arguments) {
    Preconditions.notNull(arguments, "argument array must not be null");
    return () -> arguments;
  }

  static Arguments arguments(Object... arguments) {
    return of(arguments);
  }
}

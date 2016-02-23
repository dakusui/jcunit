package com.github.dakusui.jcunit.coverage;

import java.util.List;

public interface Coverage {
  interface Metric<T> {
    String name();
    T value();
    Class<T> type();
  }

  List<Metric<?>> metrics();
}

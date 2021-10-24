package com.github.dakusui.jcunitx.core;

import static com.github.dakusui.pcond.Assertions.that;
import static com.github.dakusui.pcond.functions.Predicates.isNotNull;

public interface KeyValuePair<V> {
  static <V> KeyValuePair<V> create(String key, V value) {
    assert that(key, isNotNull());
    return new KeyValuePair<V>() {
      @Override
      public String key() {
        return key;
      }

      @Override
      public V value() {
        return value;
      }
    };
  }

  String key();

  V value();
}

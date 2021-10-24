package com.github.dakusui.jcunit.core.tuples;

public interface Tuple extends KeyValuePairs {
  static Tuple from(KeyValuePairs keyValuePairs) {
    if (keyValuePairs instanceof Tuple)
      return (Tuple) keyValuePairs;
    class Impl extends KeyValuePairs.Sorted implements Tuple {
      {
        putAll(keyValuePairs);
      }}
    return new Impl();
  }
}

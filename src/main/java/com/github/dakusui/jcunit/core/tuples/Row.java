package com.github.dakusui.jcunit.core.tuples;

public interface Row extends KeyValuePairs {
  static Row from(KeyValuePairs keyValuePairs) {
    if (keyValuePairs instanceof Row)
      return (Row) keyValuePairs;
    class Impl extends KeyValuePairs.Sorted implements Row {
      {
        putAll(keyValuePairs);
      }
    }
    return new Impl();
  }
}

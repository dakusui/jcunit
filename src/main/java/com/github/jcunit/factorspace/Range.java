package com.github.jcunit.factorspace;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * // @formatter:off
 * A class to model a "range" in a list.
 *
 * This class is used by `@From` annotation and provides its `range` functionality.
 * This class is designed to provide a similar functionality called a "slice" in Python.
 *
 * If a list `in` is given to `slice(List<?>)` method and an instance of this class is created from an `expression` string,
 * the method will return the following values.:
 *
 * ----
 * expression: "0" -> slice: in.get(0)
 * expression: "1" -> slice: in.get(1)
 * expression: "-1" -> slice: in.get(in.size() - 1)
 * expression: "0:" -> slice: in.sublist(0, in.size())
 * expression: "1:" -> slice: in.sublist(1, in.size())
 * expression: ":2" -> slice: in.sublist(0, 2)
 * ----
 *
 * Limitations:: the behavior of `Range` class is not specified for invalid expressions such as "1:0".
 *
 * // @formatter:on
 *
 * @see com.github.jcunit.annotations.From
 */
abstract public class Range {
  /**
   * Returns a slice of a given `in` array.
   * Note that this method may return a list or an element in it depending on how the `Range` object is created.
   *
   * @param in An array from which the returned value is extracted.
   * @return A slice of an `in` array.
   *
   * @see Range#of(String)
   */
  public abstract Object slice(List<?> in);

  /**
   * Creates a `Range` object from a given `range` expression.
   * @param range A string expression to specify a range in an ordered container (an array or a list) object.
   * @return A `Range` object created from the `range` expression.
   */
  public static Range of(String range) {
    requireNonNull(range);
    int i = range.indexOf(':');
    if (i < 0) return new Range() {
      final int index = parseInt(range, 0);

      @Override
      public Object slice(List<?> in) {
        return in.get(index);
      }
    };
    return new Range() {
      @SuppressWarnings("unchecked")
      @Override
      public List<Object> slice(List<?> in) {
        requireNonNull(in);
        final int begin = parseInt(range.substring(0, i), 0);
        final int end = parseInt(range.substring(i + 1), in.size());
        return (List<Object>) in.subList(begin, end);
      }
    };
  }

  private static int parseInt(String range, int fallbackForEmpty) {
    return !range.isEmpty() ? Integer.parseInt(range)
                            : fallbackForEmpty;
  }
}

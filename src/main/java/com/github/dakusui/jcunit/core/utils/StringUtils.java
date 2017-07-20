package com.github.dakusui.jcunit.core.utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit.core.utils.Checks.checknotnull;

public enum StringUtils {
  ;

  /**
   * Joins given string objects with {@code sep} using {@code formatter}.
   * <p/>
   * This method is implemented in order to reduce dependencies on external libraries.
   *
   * @param sep       A separator to be used to join {@code elemes}.
   * @param formatter A formatter used to join strings.
   * @param elems     Elements to be joined.
   * @return A joined {@code String}
   */
  @SafeVarargs
  public static <T> String join(String sep, Formatter<T> formatter,
      T... elems) {
    return Stream.of(elems)
        .map(formatter::format)
        .collect(Collectors.joining(checknotnull(sep)));
  }

  /**
   * Joins given string objects with {@code sep} using {@code Formatter.CONFIG}.
   * <p/>
   * This method is implemented in order to reduce dependencies on external libraries.
   *
   * @param sep   A separator to be used to join {@code elemes}
   * @param elems Elements to be joined.
   * @return A joined {@code String}
   */
  @SuppressWarnings("unchecked")
  public static String join(String sep, Object... elems) {
    return join(sep, Formatter.INSTANCE, elems);
  }

  @SuppressWarnings("unchecked")
  public static String join(String sep, List<?> elems) {
    return join(sep, Formatter.INSTANCE, checknotnull(elems).toArray());
  }


  public interface Formatter<T> {
    Formatter INSTANCE = (Formatter<Object>) elem ->
        elem == null ?
            null :
            elem.toString();

    String format(T elem);
  }
}

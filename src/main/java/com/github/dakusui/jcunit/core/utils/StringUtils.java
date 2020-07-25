package com.github.dakusui.jcunit.core.utils;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

public enum StringUtils {
  ;

  public static String join(String sep, List<?> elems) {
    return elems.stream().map(Objects::toString).collect(joining(sep));
  }
}

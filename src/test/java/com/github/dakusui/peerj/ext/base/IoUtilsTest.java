package com.github.dakusui.peerj.ext.base;

import java.util.List;

import static com.github.dakusui.crest.Crest.asListOf;
import static com.github.dakusui.crest.Crest.assertThat;
import static java.util.Arrays.asList;

public class IoUtilsTest {
  public static void main(String... args) {
    System.getProperties().forEach((k, v) -> System.out.println(k + "=" + v));
    List<String> actual = asList("d", "a", "b", "c");
    assertThat(actual, asListOf(String.class).containsExactly(asList("b", "c", "d", "a", "e")).$());
  }
}

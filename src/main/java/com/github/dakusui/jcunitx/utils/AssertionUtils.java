package com.github.dakusui.jcunitx.utils;

import com.github.dakusui.pcond.core.refl.MethodQuery;

import java.util.Map;
import java.util.function.Predicate;

import static com.github.dakusui.pcond.functions.Functions.chainp;
import static com.github.dakusui.pcond.functions.Functions.parameter;
import static com.github.dakusui.pcond.functions.Predicates.callp;

public enum AssertionUtils {
  ;

  public static <M extends Map<?, ?>> Predicate<M> containsKey(String key) {
    return chainp("containsKey", key);
  }

  public static <K> Predicate<String> isKeyOf(Map<K, ?> map) {
    return callp(MethodQuery.instanceMethod(map, "containsKey", parameter()));
  }
}

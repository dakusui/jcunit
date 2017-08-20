package com.github.dakusui.jcunit8.runners.helpers;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.fsm.spec.FsmSpec;
import com.github.dakusui.jcunit8.factorspace.GroupedParameterFactoryBuilder;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.factorspace.SequenceParameterFactoryBuilder;

import java.util.function.Function;

import static java.util.Arrays.asList;

public enum ParameterUtils {
  ;

  @SafeVarargs
  public static <T> Parameter.Simple.Factory<T> simple(T... values) {
    return Parameter.Simple.Factory.of(asList(values));
  }

  public static <T> Parameter.Regex.Factory<T> regex(String regex, Function<String, T> function) {
    return Parameter.Regex.Factory.of(regex, function);
  }

  public static Parameter.Regex.Factory<String> regex(String regex) {
    return regex(regex, Function.identity());
  }

  public static <T> Parameter.Fsm.Factory<T> fsm(Class<? extends FsmSpec<T>> fsmSpecClass, int scenarioLength) {
    return Parameter.Fsm.Factory.of(fsmSpecClass, scenarioLength);
  }

  @SafeVarargs
  public static <T> SequenceParameterFactoryBuilder<T> sequence(T... args) {
    return new SequenceParameterFactoryBuilder<>(asList(args));
  }

  public static <T> GroupedParameterFactoryBuilder<T> grouped(Function<Tuple, T> translator) {
    return new GroupedParameterFactoryBuilder<>(translator);
  }

  public static GroupedParameterFactoryBuilder<Tuple> grouped() {
    return grouped(Function.identity());
  }
}
package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit8.pipeline.stages.Generator.VOID;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class Args implements Serializable {
  private final Object[] values;

  public Args(Object[] values) {
    Checks.checknotnull(values);
    this.values = values;
  }

  public Object[] values() {
    return this.values;
  }

  public int size() {
    return this.values.length;
  }

  public Type[] types() {
    return Stream.of(this.values())
        .map((Function<Object, Type>) in -> in != null
            ? in.getClass()
            : null)
        .collect(toList())
        .toArray(new Type[this.values.length]);
  }

  public boolean containsVoid() {
    return Stream.of(values).anyMatch(VOID::equals);
  }

  @Override
  public String toString() {
    return format("[%s]", StringUtils.join(",", values));
  }
}

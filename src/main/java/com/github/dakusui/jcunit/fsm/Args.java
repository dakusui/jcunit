package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.StringUtils;
import com.github.dakusui.jcunit.core.Utils;

import java.io.Serializable;
import java.lang.reflect.Type;

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
    return Utils.transformLazily(
        Utils.asList(this.values),
        new Utils.Form<Object, Type>() {
          @Override
          public Type apply(Object in) {
            return in != null
                ? in.getClass()
                : null;
          }
        }).toArray(new Type[this.values.length]);
  }

  @Override
  public String toString() {
    return StringUtils.format("[%s]", StringUtils.join(",", values));
  }
}

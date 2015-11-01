package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Args implements Serializable {
  private final Object[] values;

  Args(Object[] values) {
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
    List<Type> ret = new ArrayList<Type>(this.size());
    for (Object each : this.values()) {
      ret.add(each != null ? each.getClass() : null);
    }
    return ret.toArray(new Type[this.size()]);
  }

  @Override
  public String toString() {
    return StringUtils.format("[%s]", StringUtils.join(",", values));
  }
}

package com.github.dakusui.jcunit.experimentals.fsm;

import java.util.Arrays;

public class Args {
  public static final Args EMPTY_ARGS = args();

  private final Object[] values;

  public Args(Object[] args) {
    this.values = args;
  }

  public Object[] values() {
    return this.values;
  }

  public static Args args(Object... args) {
    return new Args(args);
  }

  @Override
  public int hashCode() {
    int ret = 0;
    for (Object each : this.values) {
      ret += each != null ? each.hashCode() : 0;
    }
    return ret;
  }

  @Override
  public boolean equals(Object another) {
    if (another == null || !(another instanceof Args)) {
      return false;
    }
    return Arrays.equals(this.values, ((Args) another).values);
  }

  @Override
  public String toString() {
    return Arrays.toString(this.values);
  }
}

package com.github.dakusui.jcunit.regex;

import static com.github.dakusui.jcunit.core.utils.Checks.checknotnull;
import static java.lang.String.format;

public class Reference implements Value {
  public final String key;

  Reference(String key) {
    this.key = checknotnull(key);
  }

  @Override
  public int hashCode() {
    return this.key.hashCode();
  }

  @Override
  public boolean equals(Object another) {
    return another instanceof Reference
        && this.key.equals(((Reference) another).key);
  }

  @Override
  public String toString() {
    return format("Reference:<%s>", this.key);
  }
}

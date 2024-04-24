package com.github.dakusui.jcunit.regex;

import java.util.Objects;

import static java.lang.String.format;

class Immediate implements Value {
  final Object value;

  Immediate(Object value) {
    this.value = value;
  }

  @Override
  public int hashCode() {
    return this.value == null ?
        0 :
        this.value.hashCode();
  }

  @Override
  public boolean equals(Object another) {
    return another instanceof Immediate
        && Objects.equals(this.value, ((Immediate) another).value);
  }


  @Override
  public String toString() {
    return format("Immediate:<%s>", this.value);
  }
}

package com.github.dakusui.jcunit.core;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedList;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
  Param[] EMPTY_ARRAY = new ArrayBuilder().build();

  String[] value();

  class Builder {
    private String[] values;

    public Builder() {
    }

    public Builder add(String... values) {
      this.values = values;
      return this;
    }

    public Param build() {
      return new Param() {
        @Override
        public Class<? extends Annotation> annotationType() {
          return Param.class;
        }

        @Override
        public String[] value() {
          return Builder.this.values;
        }
      };
    }
  }

  class ArrayBuilder {
    private final List<Param> params = new LinkedList<Param>();

    public ArrayBuilder() {
    }

    public ArrayBuilder add(String... values) {
      params.add(new Builder().add(values).build());
      return this;
    }

    public Param[] build() {
      return this.params.toArray(new Param[this.params.size()]);
    }
  }
}

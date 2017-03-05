package com.github.dakusui.jcunit.runners.standard.annotations;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.plugins.Plugin;
import com.github.dakusui.jcunit.plugins.PluginUtils;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedList;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
public @interface Value {
  String[] value();

  class Builder {
    private String[] values;

    public Builder() {
    }

    public Builder add(String... values) {
      this.values = values;
      return this;
    }

    public Value build() {
      return new Value() {
        @Override
        public Class<? extends Annotation> annotationType() {
          return Value.class;
        }

        @Override
        public String[] value() {
          return Builder.this.values;
        }
      };
    }
  }

  class ArrayBuilder {
    private final List<Value> values = new LinkedList<Value>();

    public ArrayBuilder() {
    }

    public ArrayBuilder add(String... values) {
      this.values.add(new Builder().add(values).build());
      return this;
    }

    public Value[] build() {
      return this.values.toArray(new Value[this.values.size()]);
    }
  }

  /**
   * A resolver that converts {@literal @}{@code Value} into a value whose type is specified as
   * a parameter type of constructor.
   *
   * By this resolver, values given by {@literal @}{@code Value} annotation become actual values
   * that are used by plug-in implementations.
   */
  class Resolver extends Plugin.Param.Resolver<Value> {
    public Resolver() {
      super(createConverters(PluginUtils.StringArrayResolver.INSTANCE.allConverters()));
    }

    private static List<Plugin.Param.Converter<Value>> createConverters(List<Plugin.Param.Converter<String[]>> converters) {
      return Utils.transform(
          Checks.checknotnull(converters),
          new Utils.Form<Plugin.Param.Converter<String[]>, Plugin.Param.Converter<Value>>() {
            @Override
            public Plugin.Param.Converter<Value> apply(final Plugin.Param.Converter<String[]> inConverter) {
              return new Plugin.Param.Converter<Value>() {
                @Override
                public Object convert(Class requested, Value inValue) {
                  return inConverter.convert(requested, inValue.value());
                }

                @Override
                public boolean supports(Class<?> target) {
                  return inConverter.supports(target);
                }
              };
            }
          }
      );
    }

    @Override
    protected <T> Plugin.Param.Converter<Value> chooseConverter(Class<T> clazz, List<Plugin.Param.Converter<Value>> from) {
      return from.get(0);
    }
  }
}

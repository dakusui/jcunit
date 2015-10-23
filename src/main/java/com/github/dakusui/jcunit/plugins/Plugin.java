package com.github.dakusui.jcunit.plugins;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.SystemProperties;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A common interface of all plugins of JCUnit that can be configured through
 * '{@literal @}Param' annotations.
 */
public interface Plugin {
  /**
   * Default value is a randomly generated hash. This means you are not able to
   * use this string as a "normal" value of this attribute. (Limitation)
   */
  String DUMMY_STRING_FOR_DEFAULT_VALUE = "ff6e8be2297b53e749c990e3d6bdc9bf";

  @interface Param {
    enum Source {
      RUNNER,
      INSTANCE,
      SYSTEM_PROPERTY
    }

    SystemProperties.KEY propertyKey() default SystemProperties.KEY.DUMMY;

    Source source() default Source.INSTANCE;

    String defaultValue() default DUMMY_STRING_FOR_DEFAULT_VALUE;


    abstract class Translator<S> implements Cloneable {
      public static final Translator NULLTRANSLATOR = new Translator(Collections.EMPTY_LIST) {
        @Override
        protected Converter chooseConverter(Class clazz, List from) {
          return Converter.NULLCONVERTER;
        }
      };
      private List<Converter<S>> converters;

      protected Translator(List<Converter<S>> converters) {
        this.converters = new LinkedList<Converter<S>>();
        this.converters.addAll(converters);
      }

      <T> T translate(Class<T> requested, S value) {
        Checks.checknotnull(requested);
        return Checks.cast(requested, chooseConverter(requested, findCompatibleConverters(requested)).convert(requested, value));
      }

      protected <T> List<Converter<S>> findCompatibleConverters(Class<T> targetType) {
        Checks.checknotnull(targetType);
        List<Converter<S>> ret = new ArrayList<Converter<S>>(this.allConverters().size());

        for (Converter<S> each : this.allConverters()) {
          if (ReflectionUtils.isAssignable(targetType, each.outputType())) {
            ret.add(each);
          }
        }
        Checks.checkcond(ret.size() > 0);
        return ret;
      }

      abstract protected <T> Converter<S> chooseConverter(Class<T> clazz, List<Converter<S>> from);

      public Translator<S> cloneTranslator() {
        try {
          Checks.cast(this.getClass(), this.clone());
        } catch (CloneNotSupportedException e) {
          Checks.rethrow(e);
        }
        ////
        // This path should never be executed.
        throw new RuntimeException();
      };

      private List<Converter<S>> allConverters() {
        return this.converters;
      }

      public void addConverter(Converter<S> converter) {
        this.converters.add(Checks.checknotnull(converter));
      }
    }

    /**
     * @param <I> Input type. E.g., {@literal @}{@code Param}.
     */
    interface Converter<I> {
      Converter<Object> NULLCONVERTER = new Converter<Object>() {
        @Override
        public <R> R convert(Class<R> requested, Object in) {
          return null;
        }

        @Override
        public Class<?> outputType() {
          return Object.class;
        }
      };

      <R> R convert(Class<R> requested, I in);

      Class<?> outputType();
    }
  }

  abstract class Base<S> implements Plugin {
    protected final Param.Translator<S> translator;

    public Base(Param.Translator<S> translator) {
      this.translator = Checks.checknotnull(translator);
    }
  }
}

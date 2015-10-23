package com.github.dakusui.jcunit.plugins;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.standardrunner.annotations.Arg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A common interface of all plugins of JCUnit that can be configured through
 * '{@literal @}Param' annotations.
 */
public interface JCUnitPlugin {
  @interface Param {
    Class<?> value();

    boolean array() default false;

    abstract class Translator<S> implements Cloneable {
      private List<Converter<S>> converters;

      protected Translator(List<Converter<S>> converters) {
        this.converters = new LinkedList<Converter<S>>();
        this.converters.addAll(converters);
      }

      <T> T translate(Class<T> targetType, S value) {
        Checks.checknotnull(targetType);
        return Checks.cast(targetType, chooseConverter(targetType, findCompatibleConverters(targetType)).convert(value));
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
      Object convert(I in);

      Class<?> outputType();
    }
  }

  /**
   * TODO: update this javadoc by moving to somewhere.
   * Initializes this object.
   * <p/>
   * Users of the implementations of this interface must call this method right
   * after this class is instantiated.
   * <p/>
   * Until this method is called, behaviors of any other methods will not be predictable.
   * <p/>
   * The parameters ({@code processedParameters}) are values that are already
   * validated and translated into ones the users (and the plug-in) originally intended by
   * using {@code @Params} annotations.
   * <p/>
   * That is, if an annotation below is given,
   * <p/>
   * <pre>
   *  args = {
   *      {@literal @}Param("2")
   *  }),
   * </pre>
   * <p/>
   * And the {@code parameterTypes} returns
   * <p/>
   * <pre>
   *   new ParamType[]{ ParamType.Int }
   * </pre>
   * <p/>
   * then, the user's intention is to pass an int value 2 to this plug in.
   * <p/>
   * So the {@code processedParameters} will be an array whose first and only element
   * is an int, 2.
   *
   * @param params An array of parameter values.
   *               <p/>
   *               <p/>
   *               (cont...)
   *               The implementations of this method must clarify the expectations for
   *               {@code processedParams}.
   */
  void init(Object[] params);

  /**
   * Returns an array of parameter types that describes expectations a plugin (an implementation
   * of this interface) has for 'parameters' given to it through '{@literal @}Param' annotations.
   * <p/>
   * JCUnit uses the value returned by this method to validateFactorField and convert from strings appeared
   * in {@code @Param} annotations to Java objects.
   * <p/>
   * The converted values will then be given to {@code init(Object[])} method of this interface.
   * <p/>
   * An element in the returned value can have a default value if
   * <ul>
   * <li>it is the last element in the array.</li>
   * <li>or all the following elements have default values or 'varargs'.</li>
   * </ul>
   * <p/>
   * A 'varargs' element can only be placed at the last of the array and if it appears in the
   * array, it processes the corresponding param value and following values.
   * <p/>
   * This method needs to be able to be executed even before {@code init} method is executed
   * since it is used to compute values to be given to the method as its parameter
   * ({@code processedParameters}).
   * and must return the same value always regardless of the internal state of this object.
   */
  Arg.Type[] parameterTypes();

  abstract class Base implements JCUnitPlugin {
    @Override
    public void init(Object[] params) {
    }
  }
}

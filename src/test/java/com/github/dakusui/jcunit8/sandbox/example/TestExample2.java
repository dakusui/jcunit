package com.github.dakusui.jcunit8.sandbox.example;

import com.github.jcunit.annotations.*;
import com.github.jcunit.core.model.ValueResolver;
import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.runners.junit5.JCUnitTestExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.github.jcunit.core.model.ValueResolver.FromClass.classMethodNamed;
import static com.github.jcunit.core.model.ValueResolver.FromClass.findMethod;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
@Disabled
@ExtendWith(JCUnitTestExtension.class)
@UsingParameterSpace(TestExample2.class)
public class TestExample2 {
  @Named
  @JCUnitParameter
  public static List<ValueResolver<String>> param1() {
    return asList(ValueResolver.from("hello").$(),
                  ValueResolver.from("world").$(),
                  referenceTo("param3"),
                  fromInvokable(Invokable.fromClassMethodNamed(TestExample2.class, "param1Value1"))
    );
  }

  private static <T> ValueResolver<T> referenceTo(String parameterName) {
    new ValueResolver<T>() {

      @SuppressWarnings("unchecked")
      @Override
      public T resolve(Tuple testData) {
        return ((ValueResolver<T>) testData.get(parameterName)).resolve(testData);
      }

      @Override
      public List<String> dependencies() {
        return singletonList(parameterName);
      }

      @Override
      public String toString() {
        return "reference[" + parameterName + "]";
      }
    };
  }

  private static <T> ValueResolver<T> fromInvokable(Invokable<T> invokable) {
    return new ValueResolver<T>() {
      final List<String> dependencies = invokable.parameters()
                                                 .stream()
                                                 .map(p -> p.getAnnotation(From.class).value())
                                                 .collect(toList());

      @SuppressWarnings("unchecked")
      @Override
      public T resolve(Tuple testData) {
        return invokable.invoke(dependencies.stream()
                                            .map(testData::get)
                                            .map(o -> (ValueResolver<T>) o)
                                            .map(r -> r.resolve(testData))
                                            .toArray());
      }

      @Override
      public List<String> dependencies() {
        return dependencies;
      }

      @Override
      public String toString() {
        return invokable.name() + dependencies;
      }
    };
  }

  interface Invokable<T> {
    T invoke(Object... args);

    List<Parameter> parameters();

    String name();

    static <T> Invokable<T> fromClassMethodNamed(Class<?> klass, String methodName) {
      return from(null, findMethod(klass, classMethodNamed(methodName)));
    }

    static <T> Invokable<T> from(Object object, Method method) {
      return new Invokable<T>() {
        @SuppressWarnings("unchecked")
        @Override
        public T invoke(Object... args) {
          try {
            return (T) method.invoke(object, args);
          } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
          }
        }

        @Override
        public List<Parameter> parameters() {
          return Arrays.stream(method.getParameters())
                       .collect(toList());
        }

        @Override
        public String name() {
          return (object == null ? ""
                                 : object.getClass().getSimpleName() + ".")
                 + method.getName();
        }

        @Override
        public String toString() {
          return name() + parameters().stream()
                                      .map(m -> m.getType().getSimpleName())
                                      .collect(toList());
        }
      };
    }
  }

  @Named
  @JCUnitParameter
  public static List<ValueResolver<Map<String, List<String>>>> param2() {
    return asList(ValueResolver.from(Collections.singletonMap("K2", singletonList("V2"))).$(),
                  ValueResolver.from(Collections.singletonMap("K1", singletonList("V1"))).$());
  }

  @Named("param1Value1")
  @JCUnitParameterValue
  public static String param1Value1(@From("param3") String param3) {
    return "Hello, " + param3;
  }

  @Named
  @JCUnitParameter
  public static List<ValueResolver<String>> param3() {
    return asList(ValueResolver.of("Scott"),
                  ValueResolver.of("John"));
  }

  @JCUnitTest
  public void testMethod(@From("param1") String param1, @From("param2") Map<String, List<String>> param2) {
    System.out.println("param1:" + param1 + ", param2:" + param2);
  }
}

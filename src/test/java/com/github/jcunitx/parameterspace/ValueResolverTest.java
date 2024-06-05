package com.github.jcunitx.parameterspace;

import com.github.dakusui.jcunit8.testutils.TestBase;
import com.github.jcunit.annotations.From;
import com.github.jcunit.annotations.Named;
import com.github.jcunit.core.model.ValueResolver;
import com.github.jcunit.core.tuples.Tuple;
import com.github.valid8j.fluent.Expectations;
import com.github.valid8j.pcond.forms.Printables;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.function.Function;

import static com.github.valid8j.fluent.Expectations.*;
import static java.util.Collections.singletonList;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
public class ValueResolverTest extends TestBase {
  @Test
  public void resolveTest() {
    ValueResolver<Object> resolver = ValueResolver.of("hello");

    Object value = resolver.resolve(Tuple.builder().build());

    assertStatement(value(value).toBe()
                                .instanceOf(String.class)
                                .equalTo("hello"));
  }

  @Test
  public void resolveWithDependencyTest() {
    ValueResolver<Object> resolver = ValueResolver.create(tuple -> tuple.get("p1"), singletonList("p1"));

    Object value = resolver.resolve(Tuple.builder().put("p1", "hello").build());

    assertStatement(value(value).toBe()
                                .instanceOf(String.class)
                                .equalTo("hello"));
  }

  @Test
  public void fromStaticMethod() {
    ValueResolver<String> resolver = ValueResolver.from(ValueResolverTest.class).__classMethodNamed__("resolveValue");

    Object value = resolver.resolve(Tuple.builder().put("p1", "Scott Tiger").build());

    assertStatement(value(value).toBe()
                                .instanceOf(String.class)
                                .equalTo("Hello, <Scott Tiger>"));
  }

  @Test
  public void fromFunction() {
    ValueResolver<Class<?>> resolver = ValueResolver.from(ValueResolverTest.class).$("p1");
    Object value = resolver.resolve(Tuple.builder().put("p1", "hello").build());

    assertAll(value(value).toBe().instanceOf(Class.class),
              value(resolver).invoke("dependencies").asListOf(String.class)
                             .satisfies(x -> x.size().toBe().equalTo(1))
                             .satisfies(x -> x.elementAt(0).toBe().equalTo("p1")));
  }

  @Test
  public void fromFunctionNotFound() {
    Expectations.assertStatement(value(ValueResolver.from(ValueResolverTest.class))
                                     .expectException(NoSuchElementException.class, classMethodNamed("notExisting"))
                                     .getMessage()
                                     .satisfies()
                                     .containing("No matching method for")
                                     .containing("notExisting"));
  }

  private static Function<ValueResolver.FromClass, ValueResolver<Object>> classMethodNamed(String classMethodName) {
    return Printables.function("classMethodNamed[" + classMethodName + "]", x -> x.__classMethodNamed__(classMethodName));
  }

  @Named
  public static String resolveValue(@From("p1") String p1) {
    return "Hello, <" + p1 + ">";
  }
}

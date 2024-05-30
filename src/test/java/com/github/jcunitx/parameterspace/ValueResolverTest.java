package com.github.jcunitx.parameterspace;

import com.github.dakusui.jcunit8.testutils.TestBase;
import com.github.jcunit.annotations.From;
import com.github.jcunit.annotations.Named;
import com.github.jcunit.core.model.ValueResolver;
import com.github.jcunit.core.model.ValueResolvers;
import com.github.jcunit.core.tuples.Tuple;
import org.junit.jupiter.api.Test;

import static com.github.valid8j.fluent.Expectations.assertStatement;
import static com.github.valid8j.fluent.Expectations.value;
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
    ValueResolver<String> resolver = ValueResolvers.from(ValueResolverTest.class).classMethodNamed("resolveValue");

    Object value = resolver.resolve(Tuple.builder().put("p1", "Scott Tiger").build());

    assertStatement(value(value).toBe()
                                .instanceOf(String.class)
                                .equalTo("Hello, <Scott Tiger>"));
  }

  @Named
  public static String resolveValue(@From("p1") String p1) {
    return "Hello, <" + p1 + ">";
  }
}

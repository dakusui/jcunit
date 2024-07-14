package com.github.jcunit.annotations;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * // @formatter:off
 * An annotation to specify which class should be used as a **parameter space**.
 *
 * A parameter space class should define a set of methods which return a list of value resolvers (`List<ValueResolver<T>>`).
 * Following is an example of such a s method.
 *
 * [source, java]
 * ----
 *   public static class ParameterSpace {
 *     @Named
 *     @JCUnitParameter(type = Type.REGEX, args = "(scott|john)")
 *     public static List<ValueResolver<String>> param1() {
 *       return asList(
 *           ValueResolver.of("John").name("john"),
 *           ValueResolver.<String>fromInvokable(referenceTo("param3", Range.of("0"))).name("scott"));
 *     }
 *
 *     @Named
 *     @JCUnitParameter
 *     public static List<ValueResolver<String>> param3() {
 *       return singletonList(ValueResolver.of("Scott"));
 *     }
 *   }
 * ----
 *
 * This parameter space defines a couple of parameters, which are respectively named `param1` and `param2`.
 * Both are parameters whose type is a list of `String`.
 * In the current design of **JCUnitX**, a parameter is always a list in order to handle values in a list and a simple value transparently.
 * This design decision may be changed in the future versions.
 *
 * // @formatter:on
 *
 * @see Named
 * @see JCUnitParameter
 */
@Retention(RUNTIME)
public @interface UsingParameterSpace {
  Class<?> value() default Object.class;
}

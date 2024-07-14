package com.github.jcunit.annotations;

import com.github.jcunit.factorspace.Range;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Objects.requireNonNull;

/**
 * An annotation that chooses values of a parameter.
 *
 * **JCUnitX** has a concept of **ParameterSpace**, where all parameters are grouped along with their constraints.
 * The framework generates efficient test input data items automatically, and it assigns
 * A test method accepts parameters and the framework of **JCUnitX** assigns values
 *
 *
 * In **JCUnitX**, a value of a parameter is always a list even if it can only have single value.
 * This design is introduced in order to handle list-value parameters and single value parameters transparently.
 *
 * `@From` annotation specifies
 *
 * **Examples:**
 *
 * ```
 * methodName(@From(value="param1", range="0") String parameter)
 * ```
 *
 * The first element of `param1` will be assigned to the annotated parameter.
 * This is a default behavior.
 *
 * To select all the values in `param1`, you can do:
 *
 * ```
 * methodName(@From(value="param1", range="0:") String parameter)
 * ```
 *
 * This selects all the values but the last.
 *
 * ```
 * methodName(@From(value="param1", range="0:-1") String parameter)
 * ```
 *
 * To take only the last parameter, do:
 *
 * ```
 * methodName(@From(value="param1", range="-1" String parameter)
 * ```
 *
 * @see Range
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface From {
  /**
   * A name of parameter, whose value should be assigned to a parameter this annotation is attached to.
   *
   * @return A name of parameter.
   */
  String value();

  /**
   * Specifies a range in a value list, which should be assigned to an annotated parameter.
   *
   * The syntax is similar to Python's "slice".
   *
   * * All the values in the list: ```range=":"```
   * * All the values but the last: ```range=":-1"```
   * * All the values but the first (**cdr**): ```range="1:"```
   *
   * If value of this attribute doesn't contain `:`, it will be considered that a single element in the parameter value list is specified.:
   *
   * * The first element in the parameter (**car**): ```range=0```
   * * The last element in the parameter: ```range=-1```
   *
   * @return A range string.
   * @see Range
   */
  String range() default "0";
}

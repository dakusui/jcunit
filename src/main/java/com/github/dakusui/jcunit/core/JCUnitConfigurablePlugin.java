package com.github.dakusui.jcunit.core;

public interface JCUnitConfigurablePlugin {
  /**
   * Initializes this object.
   * Users of the implementations of this interface must call this method
   * after this class is instantiated.
   * <p/>
   * Until this method is called, behaviors of any other methods will not be predictable.
   *
   * The parameters ({@code processedParameters}) are values that are already
   * validated and translated into one the users originally specified by
   * using {@code @Params} annotations.
   *
   * That is, if an annotation below is given,
   *
   * <pre>
   *  params = {
  *      &at;Param(type = Type.Int, array = false, value = {"2"})
   *  }),
   * </pre>
   *
   * then, the user's intention is to pass an int value 2 to JCUnit.
   * So the {@code processedParameters} will be an array whose first and only element
   * is an int, 2.
   *
   * The implementations of this method must clarify the expectations for
   * {@code processedParameters}.
   *
   * @param processedParameters An array of processed parameter values.
   */
  public void init(Object[] processedParameters);

  /**
   * Returns an array of parameter types that describes expectations a plugin (an implementation
   * of this interface) has for 'parameters' given to it.
   *
   * JCUnit uses the value returned by this method to validate and convert from strings appeared
   * in {@code @Param} annotations to usual Java objects.
   *
   * The converted values will then be given to {@code init(Object[])} method of this interface.
   *
   * An element in the returned value can have a default value if
   * # it is the last element in the array
   * # or all the following elements have default values.
   */
  public ParamType[] parameterTypes();
}

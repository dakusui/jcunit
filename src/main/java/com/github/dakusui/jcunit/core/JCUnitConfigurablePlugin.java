package com.github.dakusui.jcunit.core;

/**
 * A common interface of all plugins of JCUnit that can be configured through
 * '{@literal @}Param' annotations.
 */
public interface JCUnitConfigurablePlugin {
  /**
   * Initializes this object.
   *
   * Users of the implementations of this interface must call this method right
   * after this class is instantiated.
   * <p/>
   * Until this method is called, behaviors of any other methods will not be predictable.
   *
   * The parameters ({@code processedParameters}) are values that are already
   * validated and translated into ones the users (and the plug-in) originally intended by
   * using {@code @Params} annotations.
   *
   * That is, if an annotation below is given,
   *
   * <pre>
   *  params = {
  *      {@literal @}Param("2")
   *  }),
   * </pre>
   *
   * And the {@code parameterTypes} returns
   *
   * <pre>
   *   new ParamType[]{ ParamType.Int }
   * </pre>
   *
   * then, the user's intention is to pass an int value 2 to this plug in.
   *
   * So the {@code processedParameters} will be an array whose first and only element
   * is an int, 2.
   *
   * @param params An array of parameter values.
   */
  void init(Param[] params);

  /**
   * Returns an array of parameter types that describes expectations a plugin (an implementation
   * of this interface) has for 'parameters' given to it through '{@literal @}Param' annotations.
   *
   * JCUnit uses the value returned by this method to validateFactorField and convert from strings appeared
   * in {@code @Param} annotations to Java objects.
   *
   * The converted values will then be given to {@code init(Object[])} method of this interface.
   *
   * An element in the returned value can have a default value if
   * <ul>
   * <li>it is the last element in the array.</li>
   * <li>or all the following elements have default values or 'varargs'.</li>
   * </ul>
   *
   * A 'varargs' element can only be placed at the last of the array and if it appears in the
   * array, it processes the corresponding param value and following values.
   *
   * This method needs to be able to be executed even before {@code init} method is executed
   * since it is used to compute values to be given to the method as its parameter
   * ({@code processedParameters}).
   * and must return the same value always regardless of the internal state of this object.
   */
  ParamType[] parameterTypes();
}

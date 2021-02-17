package com.github.dakusui.jcunitx.engine.junit5.compat;

import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.commons.util.Preconditions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;

public interface ArgumentsAccessor {

  /**
   * Get the value of the argument at the given index as an {@link Object}.
   *
   * @param index the index of the argument to get; must be greater than or
   *              equal to zero and less than {@link #size}
   * @return the value at the given index, potentially {@code null}
   */
  Object get(int index) throws ArgumentAccessException;

  /**
   * Get the value of the argument at the given index as an instance of the
   * required type.
   *
   * @param index        the index of the argument to get; must be greater than or
   *                     equal to zero and less than {@link #size}
   * @param requiredType the required type of the value; never {@code null}
   * @return the value at the given index, potentially {@code null}
   */
  <T> T get(int index, Class<T> requiredType) throws ArgumentAccessException;

  /**
   * Get the value of the argument at the given index as a {@link Character},
   * performing automatic type conversion as necessary.
   *
   * @param index the index of the argument to get; must be greater than or
   *              equal to zero and less than {@link #size}
   * @return the value at the given index, potentially {@code null}
   * @throws ArgumentAccessException if the value cannot be accessed
   *                                 or converted to the desired type
   */
  Character getCharacter(int index) throws ArgumentAccessException;

  /**
   * Get the value of the argument at the given index as a {@link Boolean},
   * performing automatic type conversion as necessary.
   *
   * @param index the index of the argument to get; must be greater than or
   *              equal to zero and less than {@link #size}
   * @return the value at the given index, potentially {@code null}
   * @throws ArgumentAccessException if the value cannot be accessed
   *                                 or converted to the desired type
   */
  Boolean getBoolean(int index) throws ArgumentAccessException;

  /**
   * Get the value of the argument at the given index as a {@link Byte},
   * performing automatic type conversion as necessary.
   *
   * @param index the index of the argument to get; must be greater than or
   *              equal to zero and less than {@link #size}
   * @return the value at the given index, potentially {@code null}
   * @throws ArgumentAccessException if the value cannot be accessed
   *                                 or converted to the desired type
   */
  Byte getByte(int index) throws ArgumentAccessException;

  /**
   * Get the value of the argument at the given index as a {@link Short},
   * performing automatic type conversion as necessary.
   *
   * @param index the index of the argument to get; must be greater than or
   *              equal to zero and less than {@link #size}
   * @return the value at the given index, potentially {@code null}
   * @throws ArgumentAccessException if the value cannot be accessed
   *                                 or converted to the desired type
   */
  Short getShort(int index) throws ArgumentAccessException;

  /**
   * Get the value of the argument at the given index as a {@link Integer},
   * performing automatic type conversion as necessary.
   *
   * @param index the index of the argument to get; must be greater than or
   *              equal to zero and less than {@link #size}
   * @return the value at the given index, potentially {@code null}
   * @throws ArgumentAccessException if the value cannot be accessed
   *                                 or converted to the desired type
   */
  Integer getInteger(int index) throws ArgumentAccessException;

  /**
   * Get the value of the argument at the given index as a {@link Long},
   * performing automatic type conversion as necessary.
   *
   * @param index the index of the argument to get; must be greater than or
   *              equal to zero and less than {@link #size}
   * @return the value at the given index, potentially {@code null}
   * @throws ArgumentAccessException if the value cannot be accessed
   *                                 or converted to the desired type
   */
  Long getLong(int index) throws ArgumentAccessException;

  /**
   * Get the value of the argument at the given index as a {@link Float},
   * performing automatic type conversion as necessary.
   *
   * @param index the index of the argument to get; must be greater than or
   *              equal to zero and less than {@link #size}
   * @return the value at the given index, potentially {@code null}
   * @throws ArgumentAccessException if the value cannot be accessed
   *                                 or converted to the desired type
   */
  Float getFloat(int index) throws ArgumentAccessException;

  /**
   * Get the value of the argument at the given index as a {@link Double},
   * performing automatic type conversion as necessary.
   *
   * @param index the index of the argument to get; must be greater than or
   *              equal to zero and less than {@link #size}
   * @return the value at the given index, potentially {@code null}
   * @throws ArgumentAccessException if the value cannot be accessed
   *                                 or converted to the desired type
   */
  Double getDouble(int index) throws ArgumentAccessException;

  /**
   * Get the value of the argument at the given index as a {@link String},
   * performing automatic type conversion as necessary.
   *
   * @param index the index of the argument to get; must be greater than or
   *              equal to zero and less than {@link #size}
   * @return the value at the given index, potentially {@code null}
   * @throws ArgumentAccessException if the value cannot be accessed
   *                                 or converted to the desired type
   */
  String getString(int index) throws ArgumentAccessException;

  /**
   * Get the number of arguments in this accessor.
   */
  int size();

  /**
   * Get all arguments in this accessor as an array.
   */
  Object[] toArray();

  /**
   * Get all arguments in this accessor as an immutable list.
   */
  List<Object> toList();

  class Default implements ArgumentsAccessor {
    private final Object[] arguments;

    public Default(Object[] arguments) {
      Preconditions.notNull(arguments, "Arguments array must not be null");
      this.arguments = arguments;
    }

    @Override
    public Object get(int index) {
      Preconditions.condition(index >= 0 && index < this.arguments.length,
          () -> format("index must be >= 0 and < %d", this.arguments.length));
      return this.arguments[index];
    }

    @Override
    public <T> T get(int index, Class<T> requiredType) {
      Preconditions.notNull(requiredType, "requiredType must not be null");
      Object value = get(index);
      try {
        Object convertedValue = ArgumentConverter.DEFAULT_INSTANCE.convert(value, requiredType);
        return requiredType.cast(convertedValue);
      } catch (Exception ex) {
        String message = format(
            "Argument at index [%d] with value [%s] and type [%s] could not be converted or cast to type [%s].",
            index, value, ClassUtils.nullSafeToString(value == null ? null : value.getClass()),
            requiredType.getName());
        throw new ArgumentAccessException(message, ex);
      }
    }

    @Override
    public Character getCharacter(int index) {
      return get(index, Character.class);
    }

    @Override
    public Boolean getBoolean(int index) {
      return get(index, Boolean.class);
    }

    @Override
    public Byte getByte(int index) {
      return get(index, Byte.class);
    }

    @Override
    public Short getShort(int index) {
      return get(index, Short.class);
    }

    @Override
    public Integer getInteger(int index) {
      return get(index, Integer.class);
    }

    @Override
    public Long getLong(int index) {
      return get(index, Long.class);
    }

    @Override
    public Float getFloat(int index) {
      return get(index, Float.class);
    }

    @Override
    public Double getDouble(int index) {
      return get(index, Double.class);
    }

    @Override
    public String getString(int index) {
      return get(index, String.class);
    }

    @Override
    public int size() {
      return this.arguments.length;
    }

    @Override
    public Object[] toArray() {
      return Arrays.copyOf(this.arguments, this.arguments.length);
    }

    @Override
    public List<Object> toList() {
      return Collections.unmodifiableList(Arrays.asList(this.arguments));
    }
  }
}

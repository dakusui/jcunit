package com.github.dakusui.jcunit8.metamodel;

import com.github.dakusui.jcunit.core.tuples.AArray;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

/**
 * A class that models a test input parameter or parameters.
 *
 * @param <T> Type of values held by this class.
 */
public interface Parameter<T> {
  /**
   * Returns a name of this parameter.
   *
   * @return A name of this parameter.
   */
  String getName();

  /**
   * Encodes this parameter into factors and constraints.
   *
   * @return Encoded factors and constraints.
   */
  FactorSpace toFactorSpace();

  /**
   * Compose a value of this parameter from a tuple.
   *
   * @param tuple an internal representation of a value of this parameter.
   * @return A value of this parameter.
   */
  T composeValue(AArray tuple);

  /**
   * Decomposes a value into an associative-array.
   *
   * @param value A value to be decomposed.
   * @return An associative-array that represents the `value`.
   */
  // TODO: Perhaps, we do not need to return `Optional` from this method, but we can just return the `Aarray`.
  Optional<AArray> decomposeValue(T value);

  /**
   * Returns a list of "known values" of this parameter.
   * In case `Simple` parameter model, the values given on its construction are returned.
   * For non-simple parameters, those are the ones the user directly specifies.
   * To implement "seeding" feature such values are important, because users are interested in the user level representation,
   * but not in the internal representation.
   *
   * @return A list of known values of this parameter.
   */
  List<T> getKnownValues();

  /**
   * A factory interface for `Parameter`.
   *
   * @param <T> The type of the parameter values (user-facing values) created by this factory.
   */
  interface Factory<T> {
    /**
     * Add an `actualValue` to this factory.
     * The implementation should register the value to the list of `known values` of the parameter.
     *
     * @param actualValue An actual value to be added to this factory.
     * @param <F>         The type of the implementation of this interface
     * @return This object.
     */
    <F extends Factory<T>> F addActualValue(T actualValue);

    @SuppressWarnings("unchecked")
    default <F extends Factory<T>> F addActualValues(List<T> actualValues) {
      actualValues.forEach(this::addActualValue);
      return (F) this;
    }

    /**
     * Create the parameter.
     * This is a builder method of this class.
     *
     * @param name The name of the parameter with which the parameter is created.
     * @return The new parameter.
     */
    Parameter<T> create(String name);

    /**
     * A base implementation class for {@code Parameter.Factory}.
     *
     * @param <T> The type of the parameter values.
     */
    abstract class Base<T> implements Factory<T> {
      protected final List<T> knownValues = new LinkedList<>();

      @SuppressWarnings("unchecked")
      @Override
      public <F extends Factory<T>> F addActualValue(T actualValue) {
        knownValues.add(actualValue);
        return (F) this;
      }
    }
  }

  /**
   * A base class for parameters.
   *
   * @param <T> The type of the parameter values
   */
  abstract class Base<T> implements Parameter<T> {
    /**
     * The name of this parameter.
     */
    protected final String name;

    /**
     * Known values of this parameter.
     */
    private final List<T> knownValues;

    /**
     * Creates an instance of this class.
     *
     * @param name        A name of this parameter.
     * @param knownValues Known values of this parameter.
     */
    protected Base(String name, List<T> knownValues) {
      this.name = requireNonNull(name);
      this.knownValues = unmodifiableList(requireNonNull(knownValues));
    }

    @Override
    public String getName() {
      return this.name;
    }

    @Override
    public FactorSpace toFactorSpace() {
      return FactorSpace.create(decompose(), generateConstraints());
    }

    @Override
    public List<T> getKnownValues() {
      return this.knownValues;
    }

    /**
     * Decomposes (or encodes) this parameter into factors, which covering array generation engines can handle.
     *
     * @return The list of factors.
     */
    protected abstract List<Factor> decompose();

    /**
     * Generates constraints over factors.
     *
     * @return The list of generated constraints.
     */
    protected abstract List<Constraint> generateConstraints();
  }
}

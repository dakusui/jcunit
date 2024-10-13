package com.github.jcunit.core.cfg;

import com.github.jcunit.utils.InternalUtils;

import java.util.Arrays;

/**
 * An interface that models grammatical elements of a BNF grammar, modeled by `Bnf` implementation object.
 */
public interface Element {
  /**
   * Requests `Parser` to process an `ongoingInput` object.
   *
   * The result should be returned as a `ParsingResult` object.
   * An implementation of this method should follow the rules below:
   *
   * - It will not modify `ongoingInput`.
   * - If the processing is successful, the returned object will return `true` for `wasSuccessful()` method call.
   * The `remainingInput()` should return a sub-list of `ongoingInput`, which has not been processed yet by `processor`.
   * - If the processing is NOT successful, the returned object will return  `false` for `wasSuccessful` method call.
   * The `remainingInput()` should be the same list as `ongoingInput`'s.
   * It may use the same object as ongoingInput for `remainingInput` for this situation.
   *
   * @param processor    A processor to process `ongoingInput`.
   * @param ongoingInput An ongoingInput to be parsed.
   * @return A ongoing parsing result.
   * @see ProcessingResult
   */
  <R extends ProcessingResult<R>> R accept(Processor<R> processor, R ongoingInput);

  /**
   * Returns a string representation of this object.
   *
   * Intended to be called from inside `toString()` method implementation.
   *
   * @return A string representation of this object.
   */
  default String stringify() {
    return InternalUtils.simpleClassName(this.getClass()) + ":" + Arrays.toString(signature());
  }

  /**
   * Returns a string which identifies this object among the same type (term, reference, concatenation, alteration)
   * of elements.
   *
   * @return A signature string.
   */
  Object[] signature();

  default boolean isEqualTo(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    return Arrays.equals(this.signature(), ((Element) o).signature());
  }
}

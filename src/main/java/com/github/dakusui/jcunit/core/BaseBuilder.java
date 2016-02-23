package com.github.dakusui.jcunit.core;

/**
 * A base interface of all builders in JCUnit.
 *
 * @param <T> Type of object to be built by this class.
 */
public interface BaseBuilder<T> {
  T build();
}

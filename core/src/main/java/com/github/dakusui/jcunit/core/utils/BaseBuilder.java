package com.github.dakusui.jcunit.core.utils;

/**
 * A model interface of all builders in JCUnit.
 *
 * @param <T> Type of object to be built by this class.
 */
public interface BaseBuilder<T> {
  <U extends T> U build();
}

package com.github.dakusui.jcunit8.ut.pipeline.testbase;

import com.github.dakusui.jcunit8.ut.UTBase;

import java.util.Collection;
import java.util.function.Predicate;

import static java.lang.String.format;

public enum UTUtils {
  ;

  public static <C extends Collection> Predicate<C> sizeIs(Predicate<Integer> predicate) {
    return UTBase.name(
        format("Size should be '%s'", predicate),
        (C tupleSet) -> predicate.test(tupleSet.size())
    );
  }

  public static <E, C extends Collection<E>> Predicate<C> allSatisfy(Predicate<E> predicate) {
    return UTBase.name(
        format("All elements should satisfy '%s'", predicate),
        (C collection) -> collection.stream().allMatch(predicate)
    );
  }
}

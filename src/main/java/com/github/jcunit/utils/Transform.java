package com.github.jcunit.utils;

import com.github.valid8j.pcond.core.printable.PrintablePredicateFactory.TransformingPredicate;
import com.github.valid8j.pcond.forms.Predicates;

import java.util.function.Function;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
public enum Transform {
  ;
  public static <O, P> TransformingPredicate.Factory<P, O> $(Function<O, P> function) {
    return Predicates.transform(function);
  }
}

package com.github.jcunit.core.cfg;

import java.util.List;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
public interface Alternation extends Element {
  List<Element> choices();
}

package com.github.dakusui.jcunit.core;

import com.github.dakusui.enumerator.tuple.AttrValue;
import com.github.dakusui.jcunit.core.tuples.Tuple;

/**
 */
public class TestCaseUtils {
  public static AttrValue<String, Object> factor(String name, Object level) {
    return new AttrValue<String, Object>(name, level);
  }

  public static Tuple newTestCase(AttrValue<String, Object>... attrs) {
    Tuple.Builder b = new Tuple.Builder();
    for (AttrValue<String, Object> attrValue : attrs) {
      b.put(attrValue.attr(), attrValue.value());
    }
    return b.build();
  }
}

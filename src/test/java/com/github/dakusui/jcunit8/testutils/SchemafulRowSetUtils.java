package com.github.dakusui.jcunit8.testutils;

import com.github.dakusui.jcunit.core.tuples.CoveringArray;
import org.hamcrest.Matcher;

import static org.junit.Assert.assertThat;

public enum SchemafulRowSetUtils {
  ;
  public static void validateSchemafulRow(CoveringArray tupleSet, Matcher<CoveringArray> matcher) {
    System.out.println("tupleSet:" + tupleSet.size());
    tupleSet.forEach(System.out::println);
    assertThat(tupleSet, matcher);
  }
}

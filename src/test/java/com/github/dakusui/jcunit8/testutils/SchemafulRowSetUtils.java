package com.github.dakusui.jcunit8.testutils;

import com.github.dakusui.jcunit8.testsuite.SchemafulRowSet;
import org.hamcrest.Matcher;

import static org.junit.Assert.assertThat;

public enum SchemafulRowSetUtils {
  ;
  public static void validateSchemafulRow(SchemafulRowSet tupleSet, Matcher<SchemafulRowSet> matcher) {
    System.out.println("tupleSet:" + tupleSet.size());
    tupleSet.forEach(System.out::println);
    assertThat(tupleSet, matcher);
  }
}

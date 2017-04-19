package com.github.dakusui.jcunit8.ut.pipeline.testbase;

import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import org.hamcrest.Matcher;

import static org.junit.Assert.assertThat;

public enum SchemafulTupleSetUtils {
  ;
  public static void validateSchemafulTupleSet(SchemafulTupleSet tupleSet, Matcher<SchemafulTupleSet> matcher) {
    tupleSet.forEach(System.out::println);
    assertThat(tupleSet, matcher);
  }
}

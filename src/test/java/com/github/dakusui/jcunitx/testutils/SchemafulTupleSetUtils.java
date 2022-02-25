package com.github.dakusui.jcunitx.testutils;

import com.github.dakusui.jcunitx.testsuite.SchemafulAArraySet;
import org.hamcrest.Matcher;

import static org.junit.Assert.assertThat;

public enum SchemafulTupleSetUtils {
  ;
  public static void validateSchemafulTupleSet(SchemafulAArraySet tupleSet, Matcher<SchemafulAArraySet> matcher) {
    System.out.println("tupleSet:" + tupleSet.size());
    tupleSet.forEach(System.out::println);
    assertThat(tupleSet, matcher);
  }
}

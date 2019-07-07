package com.github.dakusui.jcunit8.experiments.join;

import org.junit.Test;

import java.util.stream.Stream;

import static com.github.dakusui.crest.Crest.allOf;
import static com.github.dakusui.crest.Crest.asInteger;
import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.crest.Crest.call;

public class ActsUtilsTest {
  @Test
  public void testReadTestSuiteFromCsv() {
    assertThat(
        ActsUtils.readTestSuiteFromCsv(readCsv()),
        allOf(
            asInteger("size").eq(10).$(),
            asString(call("get", 0).andThen("get", "PREFIX-0").$()).equalTo("0").$(),
            asString(call("get", 9).andThen("get", "PREFIX-9").$()).equalTo("1").$()
        ));
  }

  private static Stream<String> readCsv() {
    return Stream.of(
        "# ACTS Test Suite Generation: Sun Apr 07 17:57:26 JST 2019",
        "#  '*' represents don't care value ",
        "# Degree of interaction coverage: ",
        "# Number of parameters: 10",
        "# Maximum number of values per parameter: 2",
        "# Number ofconfigurations: 10",
        "PREFIX-0,PREFIX-1,PREFIX-2,PREFIX-3,PREFIX-4,PREFIX-5,PREFIX-6,PREFIX-7,PREFIX-8,PREFIX-9",
        "0,0,1,1,1,1,1,1,1,1",
        "0,1,0,0,0,0,0,0,0,0",
        "1,0,0,1,0,1,0,1,0,1",
        "1,1,1,0,1,0,1,0,1,0",
        "1,0,1,0,0,1,0,0,1,1",
        "1,1,0,1,1,0,1,1,0,0",
        "0,0,1,1,1,0,0,0,0,1",
        "1,1,0,0,0,1,1,1,1,0",
        "1,0,0,1,0,1,1,0,0,0",
        "0,1,1,1,1,1,0,1,0,1");
  }

}

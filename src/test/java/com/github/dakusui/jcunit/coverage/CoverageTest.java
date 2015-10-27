package com.github.dakusui.jcunit.coverage;

import com.github.dakusui.combinatoradix.tuple.AttrValue;
import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.ututils.UTUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CoverageTest {
  @Before
  public void before() {
    UTUtils.configureStdIOs();
  }

  @Test
  public void test() {
    List<Tuple> testSuite = new TestSuiteBuilder()
        .add($("F1", "l12"), $("F2", "l21"), $("F3", "l31"))
        .add($("F1", "l11"), $("F2", "l22"), $("F3", "l31"))
        .add($("F1", "l11"), $("F2", "l21"), $("F3", "l32"))
        .add($("F1", "l11"), $("F2", "l21"), $("F3", "l31"))
        .build();
    Factors factorSpace = new Factors.Builder()
        .add("F1", "l11", "l12")
        .add("F2", "l21", "l22")
        .add("F3", "l31", "l32")
        .build();
    Coverage[] coverages = Coverage.examineTestSuite(testSuite, factorSpace, 3);
    for (Coverage each : coverages) {
      each.printReport(UTUtils.stdout());
    }
  }

  static class TestSuiteBuilder {
    List<Tuple> testCases = new LinkedList<Tuple>();

    TestSuiteBuilder add(AttrValue... values) {
      Tuple.Builder b = new Tuple.Builder();
      for (AttrValue each : values) {
        b.put(Checks.cast(String.class, each.attr()), each.value());
      }
      this.testCases.add(b.build());
      return this;
    }

    List<Tuple> build() {
      return Collections.unmodifiableList(testCases);
    }
  }

  static AttrValue<String, Object> $(String name, Object value) {
    return new AttrValue<String, Object>(name, value);
  }
}

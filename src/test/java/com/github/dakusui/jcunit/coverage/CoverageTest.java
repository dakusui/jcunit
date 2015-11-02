package com.github.dakusui.jcunit.coverage;

import com.github.dakusui.combinatoradix.tuple.AttrValue;
import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintBase;
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
        .add($("F1", "L2"), $("F2", "L1"), $("F3", "L1"))
        .add($("F1", "L1"), $("F2", "L2"), $("F3", "L1"))
        .add($("F1", "L1"), $("F2", "L1"), $("F3", "L2"))
        .add($("F1", "L1"), $("F2", "L1"), $("F3", "L1"))
        .build();
    Factors factorSpace = new Factors.Builder()
        .add("F1", "L1", "L2")
        .add("F2", "L1", "L2")
        .add("F3", "L1", "L2")
        .build();
    Coverage.examime(
        testSuite,
        new Coverage.TestSpace(
            factorSpace,
            3,
            new ConstraintBase() {
              @Override
              public boolean check(Tuple tuple) throws UndefinedSymbol {
                Checks.checksymbols(tuple, "F1", "F2");
                return !Utils.eq(tuple.get("F1"), tuple.get("F2"));
              }
            }
        ),
        new Coverage.Report.Printer(UTUtils.stdout())
    );
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

package com.github.dakusui.jcunit8.ut.parameters;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.testsuite.TestCase;
import org.junit.Test;

import static java.util.Arrays.asList;

public class SimpleTest extends PipelineTestBase {
  @Test
  public void test() {
    for (TestCase<Tuple> each : generateTestSuite(
        Parameter.Simple.Factory.of(asList("A1", "A2", "A3")).create("A"),
        Parameter.Simple.Factory.of(asList("B1", "B2", "B3")).create("B")
    )) {
      System.out.println(each.get());
    }
  }
}

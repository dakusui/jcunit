package com.github.dakusui.jcunit.irregex.expressions;

import com.github.dakusui.jcunit.framework.TestCase;
import com.github.dakusui.jcunit.framework.TestSuite;

import static java.lang.String.format;
import static org.junit.Assert.assertNotNull;

public class Example2 {
  public static void main(String... args) {
    TestSuite.Typed<RegexExample> testSuite = TestSuite.Typed.generate(RegexExample.class);
    for (int i = 0; i < testSuite.size(); i++) {
      RegexExample each = testSuite.inject(i);
      System.out.println(each);
      assertNotNull(each.regex);
    }
    for (TestCase each : testSuite) {
      System.out.println(format("%s; %s", each.getCategory(), each.getTuple()));
    }
  }
}

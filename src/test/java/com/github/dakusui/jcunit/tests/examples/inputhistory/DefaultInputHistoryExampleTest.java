package com.github.dakusui.jcunit.tests.examples.inputhistory;


import com.github.dakusui.jcunit.examples.inputhistory.DefaultInputHistoryExample;
import com.github.dakusui.jcunit.testutils.Metatest;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class DefaultInputHistoryExampleTest {
  public static class AllPassing extends Metatest {
    public AllPassing() {
      super(DefaultInputHistoryExample.AllPassing.class, 17, 0, 0);
    }

    @Test
    public void testPassing() {
      runTests();
    }
  }
  public static class Failing extends Metatest {
    public Failing() {
      super(DefaultInputHistoryExample.Failing.class, 17, 13, 0);
    }

    @Test
    public void testFailing() {
      runTests();
    }
  }
}

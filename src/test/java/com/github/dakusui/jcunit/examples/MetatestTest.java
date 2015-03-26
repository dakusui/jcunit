package com.github.dakusui.jcunit.examples;

import com.github.dakusui.jcunit.metatest.Metatest;
import junit.framework.AssertionFailedError;
import org.junit.Assert;
import org.junit.Test;

public class MetatestTest extends Metatest {
  public static class T1 {
    // Should fail
    @Test
    public void test1() throws Exception {
      throw new Exception();
    }

    // Should fail
    @Expected(passing=true)
    @Test
    public void test2() throws Exception {
      throw new Exception();
    }

    // Should pass
    @Expected(passing=false)
    @Test
    public void test3() throws Exception {
      throw new RuntimeException();
    }

    // Should pass
    @Expected(passing=false, exception = RuntimeException.class)
    @Test
    public void test4() throws Exception {
      throw new RuntimeException();
    }

    // Should pass
    @Expected(passing=false, exception = Exception.class)
    @Test
    public void test5() throws Exception {
      throw new RuntimeException();
    }

    // Should fail (exception mismatch)
    @Expected(passing=false, exception = NullPointerException.class)
    @Test
    public void test6() throws Exception {
      throw new RuntimeException();
    }

    // Should pass
    @Expected(passing=false, exception = RuntimeException.class, messagePattern = "A")
    @Test
    public void test7() throws Exception {
      throw new RuntimeException("A");
    }

    // Should pass (The pattern matches. A regular expression)
    @Expected(passing=false, exception = RuntimeException.class, messagePattern = ".")
    @Test
    public void test8() throws Exception {
      throw new RuntimeException("A");
    }

    // Should fail (The pattern doesn't match)
    @Expected(passing=false, exception = RuntimeException.class, messagePattern = "B")
    @Test
    public void test9() throws Exception {
      throw new RuntimeException("A");
    }

  }

  @Test
  public void test_test1() {
    // Should fail because exception is thrown
    execAndVerifyTestExpectingToFail(T1.class, "test1","The test result for '%s' didn't meet the expectation.");
  }

  @Test
  public void test_test2() {
    execAndVerifyTestExpectingToFail(T1.class, "test2", "The test result for '%s' didn't meet the expectation.");
  }

  @Test
  public void test_test3() {
    execAndVerifyTestExpectingToPass(T1.class, "test3");
  }

  @Test
  public void test_test4() {
    execAndVerifyTestExpectingToPass(T1.class, "test4");
  }

  @Test
  public void test_test5() {
    execAndVerifyTestExpectingToPass(T1.class, "test5");
  }

  @Test
  public void test_test6() {
    execAndVerifyTestExpectingToFail(
        T1.class,
        "test6",
        "An instance of 'java.lang.NullPointerException' was expected to be thrown but 'java.lang.RuntimeException' was thrown]"
    );
  }

  @Test
  public void test_test7() {
    execAndVerifyTestExpectingToPass(T1.class, "test7");
  }

  @Test
  public void test_test8() {
    execAndVerifyTestExpectingToPass(T1.class, "test8");
  }

  @Test
  public void test_test9() {
    execAndVerifyTestExpectingToFail(T1.class, "test9", "Message should match the pattern: 'B' but didn't. The actual message was 'A'");
  }


  /**
   * Executes and verifies the specified test method with expectation of failure of
   * the test case under the test.
   * The message stored in the thrown {@code AssertionFailedError} will be examined
   * if it matches with the string composed from {@code f}, {@code testClass}, and {@code testMethod}
   * If {@code f} doesn't contain '%s', the value itself will be used.
   * If {@code null} is given as {@code f}, this step will not be performed.
   *
   * @param testClass A class the test method belongs to.
   * @param testMethod A name of the method to be executed and verified.
   * @param f A format string whose the first and the only '%s' inside it will be replaced with the name of the test case.
   *
   * @see String#format
   */
  private void execAndVerifyTestExpectingToFail(Class<?> testClass, String testMethod, String f) {
    String testName=String.format("%s#%s", testClass.getCanonicalName(), testMethod);
    boolean expectedExceptionThrown = false;
    try {
      executeTestMethod(testClass, testMethod);
    } catch (AssertionFailedError e) {
      expectedExceptionThrown = true;
      String expectedMsg = String.format(f, testName);
      Assert.assertTrue(
          String.format("The expected string was not contained in the actual message.: expected='%s', actual='%s'", expectedMsg, e.getMessage()),
          e.getMessage().contains(expectedMsg)
      );
    } finally {
      if (!expectedExceptionThrown) Assert.fail();
    }
  }

  private void execAndVerifyTestExpectingToPass(Class<?> testClass, String testMethod) {
    try {
      executeTestMethod(testClass, testMethod);
    } catch (MetatestAssertionFailedError e) {
      throw new AssertionFailedError("MetatestAssertionFailedError was unexpectedly thrown.: " + e.getMessage());
    }
  }
}

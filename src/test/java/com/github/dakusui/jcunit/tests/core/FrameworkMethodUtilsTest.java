package com.github.dakusui.jcunit.tests.core;

import com.github.dakusui.jcunit.runners.standard.annotations.Condition;
import com.github.dakusui.jcunit.runners.standard.annotations.CustomTestCases;
import com.github.dakusui.jcunit.runners.standard.annotations.Precondition;
import com.github.dakusui.jcunit.runners.standard.annotations.When;
import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.runners.standard.FrameworkMethodUtils;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;
import org.junit.validator.AnnotationValidator;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class FrameworkMethodUtilsTest {
  public static class RetrieverTestClass {
    @SuppressWarnings("unused")
    @Condition
    public boolean precondition1() {
      return true;
    }

    @SuppressWarnings("unused")
    @Condition
    public boolean precondition2() {
      return true;
    }

    @SuppressWarnings("unused")
    @Condition
    public boolean precondition3() {
      return true;
    }

    @SuppressWarnings("unused")
    @Condition
    boolean invalidPrecondition1() {
      return true;
    }

    @SuppressWarnings("unused")
    @Condition
    public Boolean invalidPrecondition2() {
      return true;
    }

    @SuppressWarnings("unused")
    @Condition
    public static boolean invalidPrecondition3() {
      return true;
    }

    @SuppressWarnings("unused")
    @Condition
    public boolean invalidPrecondition4(String invalidParam) {
      return true;
    }

    @SuppressWarnings("unused")
    @Condition
    public boolean invalidPrecondition5() {
      return true;
    }

    @SuppressWarnings("unused")
    @Condition
    public boolean invalidPrecondition5(String testClass) {
      return true;
    }

    @Test
    @When("precondition1")
    public void scenario1() {
    }

    @Test
    @When("precondition2&&precondition3")
    public void scenario2() {
    }

    @Test
    @When("!precondition2&&precondition3")
    public void scenario3() {
    }

    @Test
    @When("precondition2&&!precondition3")
    public void scenario4() {
    }

    @Test
    @When({ "!precondition2&&!precondition3", "precondition1" })
    public void scenario5() {
    }

    @Test
    @When("invalidPrecondition1")
    public void invalidScenario1() {
    }

    @Test
    @When("invalidPrecondition2")
    public void invalidScenario2() {
    }

    @Test
    @When("invalidPrecondition3")
    public void invalidScenario3() {
    }

    @Test
    @When("invalidPrecondition4")
    public void invalidScenario4() {
    }

    @Test
    @When("invalidPrecondition5")
    public void invalidScenario5() {
    }

    @Test
    @When("undefinedPrecondition")
    public void invalidScenario6() {
    }
  }

  public static class TestClass2 {
    @SuppressWarnings("unused")
    @Precondition
    public boolean precondition1() {
      return true;
    }
  }

  public static class TestClass3 {
    @SuppressWarnings("unused")
    @CustomTestCases
    public static List<TestClass3> customTestCases() {
      return new LinkedList<TestClass3>();
    }
  }

  @Test
  public void testMethodsReferredToByWhenAnnotation() throws Throwable {
    List<FrameworkMethod> methodList = FrameworkMethodUtils.findReferencedFrameworkMethods(new TestClass(RetrieverTestClass.class), new When() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return When.class;
      }

      @Override
      public String[] value() {
        return new String[]{"precondition1"};
      }
    });
    assertTrue(methodListContainsItemWhoseNameIsSpecified(methodList, "precondition1"));
    List<String> emptyList = Collections.emptyList();
    AnnotationValidator validator = new When.Validator();
    Class<?> testClass = RetrieverTestClass.class;
    assertEquals(emptyList, validateMethod(validator, testClass, getFrameworkMethodByNameFromList(methodList, "precondition1")));
    assertEquals(emptyList, validateMethod(validator, testClass, getFrameworkMethodByNameFromList(methodList, "(precondition2&&precondition3)")));
    assertEquals(emptyList, validateMethod(validator, testClass, getFrameworkMethodByNameFromList(methodList, "(!precondition2&&precondition3)")));
    assertEquals(
        "The method 'invalidPrecondition1' must be public. (in com.github.dakusui.jcunit.tests.core.FrameworkMethodUtilsTest.RetrieverTestClass)",
        assertErrorSizeIsOne(validateMethod(validator, testClass, getFrameworkMethodByNameFromList(methodList, "invalidPrecondition1"))).get(0));
    assertEquals(
        "The method 'invalidPrecondition2' must return a boolean value, but 'java.lang.Boolean' is returned. (in com.github.dakusui.jcunit.tests.core.FrameworkMethodUtilsTest.RetrieverTestClass)",
        assertErrorSizeIsOne(validateMethod(validator, testClass, getFrameworkMethodByNameFromList(methodList, "invalidPrecondition2"))).get(0));
    assertEquals(
        "The method 'invalidPrecondition3' must not be static. (in com.github.dakusui.jcunit.tests.core.FrameworkMethodUtilsTest.RetrieverTestClass)",
        assertErrorSizeIsOne(validateMethod(validator, testClass, getFrameworkMethodByNameFromList(methodList, "invalidPrecondition3"))).get(0));
    assertEquals(
        "The method 'invalidPrecondition4' must not have any parameter. (in com.github.dakusui.jcunit.tests.core.FrameworkMethodUtilsTest.RetrieverTestClass)",
        assertErrorSizeIsOne(validateMethod(validator, testClass, getFrameworkMethodByNameFromList(methodList, "invalidPrecondition4"))).get(0));
    assertEquals(
        "The method 'invalidPrecondition5' is not found or not unique in a class 'com.github.dakusui.jcunit.tests.core.FrameworkMethodUtilsTest.RetrieverTestClass'",
        assertErrorSizeIsOne(validateMethod(validator, testClass, getFrameworkMethodByNameFromList(methodList, "invalidPrecondition5"))).get(0));
    assertEquals(
        "The method 'undefinedPrecondition' is not found or not unique in a class 'com.github.dakusui.jcunit.tests.core.FrameworkMethodUtilsTest.RetrieverTestClass'",
        assertErrorSizeIsOne(validateMethod(validator, testClass, getFrameworkMethodByNameFromList(methodList, "undefinedPrecondition"))).get(0));

    RetrieverTestClass testObj = new RetrieverTestClass();
    assertEquals(true, getFrameworkMethodByNameFromList(methodList, "precondition1").invokeExplosively(testObj));
    assertEquals(true, getFrameworkMethodByNameFromList(methodList, "(precondition2&&precondition3)").invokeExplosively(testObj));
    assertEquals(false, getFrameworkMethodByNameFromList(methodList, "(!precondition2&&precondition3)").invokeExplosively(testObj));
    assertEquals(false, getFrameworkMethodByNameFromList(methodList, "(precondition2&&!precondition3)").invokeExplosively(testObj));
    assertEquals(true, getFrameworkMethodByNameFromList(methodList, "((!precondition2&&!precondition3)||precondition1)").invokeExplosively(testObj));
  }

  @Test
  public void testMethodsAnnotatedWithPrecondition() throws Throwable {
    List<FrameworkMethod> methodList = FrameworkMethodUtils.FrameworkMethodRetriever.PRECONDITION.getMethods(TestClass2.class);
    assertTrue(methodListContainsItemWhoseNameIsSpecified(methodList, "precondition1"));
    TestClass2 testObj = new TestClass2();
    assertEquals(true, getFrameworkMethodByNameFromList(methodList, "precondition1").invokeExplosively(testObj));
    assertEquals(0, validateMethod(new Precondition.Validator(), TestClass2.class, getFrameworkMethodByNameFromList(methodList, "precondition1")).size());
  }

  @Test
  public void testMethodsAnnotatedWithCustomTestCases() throws Throwable {
    List<FrameworkMethod> methodList = FrameworkMethodUtils.FrameworkMethodRetriever.CUSTOM_TESTCASES.getMethods(TestClass3.class);
    assertTrue(methodListContainsItemWhoseNameIsSpecified(methodList, "customTestCases"));
    assertEquals(new LinkedList<TestClass3>(), getFrameworkMethodByNameFromList(methodList, "customTestCases").invokeExplosively(null));
    assertEquals(0, validateMethod(new CustomTestCases.Validator(), TestClass3.class, getFrameworkMethodByNameFromList(methodList, "customTestCases")).size());
  }

  private List<Throwable> validateMethod(AnnotationValidator validator, Class<?> testClass, FrameworkMethod method) {
    Checks.checknotnull(method);
    List<Throwable> errors = new LinkedList<Throwable>();
    FrameworkMethodUtils.validateFrameworkMethod(testClass, method, validator, errors);
    return errors;
  }

  private List<Throwable> assertErrorSizeIsOne(List<Throwable> errors) {
    assertEquals(String.format("Unexpected number of errors are found:%s", errors), 1, errors.size());
    return errors;
  }

  private boolean methodListContainsItemWhoseNameIsSpecified(List<FrameworkMethod> methodList, String methodName) {
    for (FrameworkMethod each : methodList) {
      if (each.getName().equals(methodName)) {
        return true;
      }
    }
    return false;
  }

  private FrameworkMethod getFrameworkMethodByNameFromList(List<FrameworkMethod> methodList, String methodName) {
    for (FrameworkMethod each : methodList) {
      if (each.getName().equals(methodName)) {
        return each;
      }
    }
    throw new AssertionError("Not found:" + methodName);
  }
}

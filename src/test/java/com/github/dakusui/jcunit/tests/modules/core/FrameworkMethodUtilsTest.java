package com.github.dakusui.jcunit.tests.modules.core;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.StringUtils;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.InvalidTestException;
import com.github.dakusui.jcunit.runners.core.TestCase;
import com.github.dakusui.jcunit.runners.standard.CompositeFrameworkMethod;
import com.github.dakusui.jcunit.runners.standard.FrameworkMethodUtils;
import com.github.dakusui.jcunit.runners.standard.annotations.Condition;
import com.github.dakusui.jcunit.runners.standard.annotations.When;
import com.github.dakusui.jcunit.testutils.UTUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;
import org.junit.validator.AnnotationValidator;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

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

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Test
  public void testSimpleMethodReference() {
    testValidationForMethodAnnotatedWithWhen(
        Utils.asList("precondition1"),
        UTUtils.<String, List<String>>toMap(
            UTUtils.entry("precondition1", Utils.asList())
        ));
  }

  @Test
  public void testSingleTermMethodReference() {
    testValidationForMethodAnnotatedWithWhen(
        Utils.asList("precondition2&&precondition3"),
        UTUtils.<String, List<String>>toMap(
            UTUtils.entry("precondition2", Utils.asList()),
            UTUtils.entry("precondition3", Utils.asList()
            )));
  }

  @Test
  public void testSingleTermWithNegateMethodReference() {
    testValidationForMethodAnnotatedWithWhen(
        Utils.asList("!precondition2&&precondition3"),
        UTUtils.<String, List<String>>toMap(
            UTUtils.entry("precondition2", Utils.asList()),
            UTUtils.entry("precondition3", Utils.asList()
            )));
  }

  @Test
  public void testSimpleMethodReferenceToInvalidMethod_nonPublic() {
    String methodName = "invalidPrecondition1";
    testValidationForMethodAnnotatedWithWhen(
        Utils.asList(methodName),
        UTUtils.<String, List<String>>toMap(
            UTUtils.entry(
                methodName,
                Utils.asList("The method 'invalidPrecondition1' must be public. (in com.github.dakusui.jcunit.tests.modules.core.FrameworkMethodUtilsTest.RetrieverTestClass)")
            )));
  }

  @Test
  public void testSimpleMethodReferenceToInvalidMethod_typeMismatch() {
    String methodName = "invalidPrecondition2";
    testValidationForMethodAnnotatedWithWhen(
        Utils.asList(methodName),
        UTUtils.<String, List<String>>toMap(
            UTUtils.entry(
                methodName,
                Utils.asList(
                    "The method 'invalidPrecondition2' must return a boolean value, but 'java.lang.Boolean' is returned. (in com.github.dakusui.jcunit.tests.modules.core.FrameworkMethodUtilsTest.RetrieverTestClass)"
                ))));
  }

  @Test
  public void testSimpleMethodReferenceToInvalidMethod_nonStatic() {
    String methodName = "invalidPrecondition3";
    testValidationForMethodAnnotatedWithWhen(
        Utils.asList(methodName),
        UTUtils.<String, List<String>>toMap(
            UTUtils.entry(
                methodName,
                Utils.asList(
                    "The method 'invalidPrecondition3' must not be static. (in com.github.dakusui.jcunit.tests.modules.core.FrameworkMethodUtilsTest.RetrieverTestClass)"
                ))));
  }

  @Test
  public void testSimpleMethodReferenceToInvalidMethod_nonParameterless() {
    String methodName = "invalidPrecondition4";
    testValidationForMethodAnnotatedWithWhen(
        Utils.asList(methodName),
        UTUtils.<String, List<String>>toMap(
            UTUtils.entry(
                methodName,
                Utils.asList(
                    "The method 'invalidPrecondition4' must not have any parameter. (in com.github.dakusui.jcunit.tests.modules.core.FrameworkMethodUtilsTest.RetrieverTestClass)"
                ))));
  }

  @Test
  public void testSimpleMethodReferenceToInvalidMethod_duplicateName() {
    expectedEx.expect(InvalidTestException.class);
    expectedEx.expectMessage(
        "The method 'invalidPrecondition5' is not unique in class 'FrameworkMethodUtilsTest$RetrieverTestClass'"
    );
    String methodName = "invalidPrecondition5";
    testValidationForMethodAnnotatedWithWhen(
        Utils.asList(methodName),
        UTUtils.<String, List<String>>toMap(
            UTUtils.entry(
                methodName,
                Utils.asList(
                    "<<SHOULD_NOT_BE_EVALUATED>>"
                ))));
  }


  @Test
  public void testSimpleMethodReferenceToInvalidMethod_undefined() {
    expectedEx.expect(InvalidTestException.class);
    expectedEx.expectMessage(
        "The method 'undefinedPrecondition' is not found in class 'FrameworkMethodUtilsTest$RetrieverTestClass'"
    );
    String methodName = "undefinedPrecondition";
    testValidationForMethodAnnotatedWithWhen(
        Utils.asList(methodName),
        UTUtils.<String, List<String>>toMap(
            UTUtils.entry(
                methodName,
                Utils.asList(
                    "<<SHOULD_NOT_BE_EVALUATED>>"
                ))));
  }

  public void testValidationForMethodAnnotatedWithWhen(
      final List<String> ref,
      final Map<String, List<String>> expectations
  ) {
    CompositeFrameworkMethod matchingMethod = findMethodReferencedByWhenAnnotation(
        RetrieverTestClass.class,
        ref
    );
    assertEquals(
        StringUtils.join("||", Utils.transform(ref,
            new Utils.Form<String, Object>() {
              @Override
              public Object apply(String in) {
                return in.contains("&&")
                    ? "(" + in + ")"
                    : in;
              }
            }
        )),
        matchingMethod.getName());

    List<FrameworkMethod> methods = FrameworkMethodUtilsTestHelper.findReferencedFrameworkMethods(
        new TestClass(RetrieverTestClass.class),
        new When() {

          @Override
          public Class<? extends Annotation> annotationType() {
            return When.class;
          }

          @Override
          public String[] value() {
            return ref.toArray(new String[ref.size()]);
          }

          @Override
          public TestCase.Type[] type() {
            return TestCase.Type.values();
          }
        });
    Map<String, List<String>> expect = Utils.newMap(expectations);
    for (FrameworkMethod each : methods) {
      String eachMethodName = each.getName();
      assertEquals(
          Checks.checknotnull(
              expect.get(eachMethodName),
              "'%s' is NOT expected to be validated", eachMethodName
          ),
          validateMethod(
              new Condition.Validator(),
              each
          )
      );
      expect.remove(eachMethodName);
    }
    assertTrue(
        "Those should have been validated but not:" + expect,
        expect.isEmpty());
  }

  private CompositeFrameworkMethod findMethodReferencedByWhenAnnotation(
      Class<?> testClass,
      final List<String> annotaionValues) {
    return FrameworkMethodUtils.buildCompositeFrameworkMethod(new TestClass(testClass), new When() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return When.class;
      }

      @Override
      public String[] value() {
        return annotaionValues.toArray(new String[annotaionValues.size()]);
      }

      @Override
      public TestCase.Type[] type() {
        return TestCase.Type.values();
      }
    });
  }


  private List<String> validateMethod(AnnotationValidator validator, FrameworkMethod method) {
    return Utils.transform(
        Checks.checknotnull(validator).validateAnnotatedMethod(method),
        new Utils.Form<Exception, String>() {
          @Override
          public String apply(Exception in) {
            return in.getMessage();
          }
        }
    );
  }
}

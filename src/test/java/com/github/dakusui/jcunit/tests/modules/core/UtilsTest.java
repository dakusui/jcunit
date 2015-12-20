package com.github.dakusui.jcunit.tests.modules.core;

import com.github.dakusui.jcunit.core.StringUtils;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import org.junit.Test;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.Assert.*;

public class UtilsTest {
  /**
   * This field is referenced by a test for Utils.validateFactorField
   */
  @SuppressWarnings("unused")
  public String invalidScopeFactorField;

  @Retention(RetentionPolicy.RUNTIME)
  public @interface Ann1 {
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface Ann2 {
  }

  static class TestClass {
    @Ann1
    @SuppressWarnings("unused")
    public  Object f = new Object();
    @Ann1
    @SuppressWarnings("unused")
    private Object g = new Object();
    @Ann1
    @Ann2
    @SuppressWarnings("unused")
    public  Object h = new Object();
    @SuppressWarnings("unused")
    public  Object i = new Object();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void normalTestValidField1() {
    TestClass testObj = new TestClass();
    assertEquals("f", ReflectionUtils.getField(testObj, "f", Ann1.class).getName());
    assertEquals("h", ReflectionUtils.getField(testObj, "h", Ann1.class).getName());
  }

  @Test
  public void normalTestValidField2() {
    TestClass testObj = new TestClass();
    //noinspection unchecked
    assertEquals("h", ReflectionUtils.getField(testObj, "h", Ann1.class, Ann2.class).getName());
  }

  @Test
  public void normalTestValidField3() {
    TestClass testObj = new TestClass();
    //noinspection unchecked
    assertEquals("i", ReflectionUtils.getField(testObj, "i").getName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void abnormalTestUndefinedField() {
    TestClass testObj = new TestClass();
    //noinspection unchecked
    ReflectionUtils.getField(testObj, "e", Ann1.class).getName();
  }

  @Test(expected = IllegalArgumentException.class)
  public void abnormalTestFieldNotAnnotated() {
    TestClass testObj = new TestClass();
    //noinspection unchecked
    ReflectionUtils.getField(testObj, "i", Ann1.class).getName();
  }

  @Test
  public void abnormalTestFieldInsufficientAnnotations() {
    TestClass testObj = new TestClass();
    //noinspection unchecked
    ReflectionUtils.getField(testObj, "f", Ann1.class, Ann2.class).getName();
  }

  public static class TestClass2 {
    // accessed through reflection.
    @SuppressWarnings("unused")
    private Object f;
  }

  @Test
  public void abnormalTestSetFieldValueToPrivateField() throws NoSuchFieldException {
    Field f = TestClass2.class.getDeclaredField("f");
    ReflectionUtils.setFieldValue(new TestClass2(), f, "Helloworld");
  }


  @SuppressWarnings("unchecked")
  @Test
  public void defaultFormatterTest() {
    assertEquals(null, StringUtils.Formatter.INSTANCE.format(null));
    assertEquals("hello", StringUtils.Formatter.INSTANCE.format("hello"));
  }

  @SuppressWarnings("unused")
  public abstract static class C {
  }

  @Test(expected = JCUnitException.class)
  public void givenAbstractClass$whenPerformCreateNewInstanceUsingNoParamConstructor$thenJCUnitExceptionWillBeThrown() {
    ReflectionUtils.create(C.class);
  }

  @SuppressWarnings("unused")
  public static class C2 extends C {
    private C2() {
    }
  }

  @Test(expected = JCUnitException.class)
  public void givenClassWhoseNonParamConstructorIsPrivate$whenPerformCreateNewInstanceUsingNoParamConstructor$thenJCUnitExceptionWillBeThrown() {
    ReflectionUtils.create(C2.class);
  }

  @SuppressWarnings("unused")
  public static class C3 extends C {
    private C3(String s) {
    }
  }

  @Test(expected = JCUnitException.class)
  public void givenClassWhichDoesntHaveNonParamConstructor$whenPerformCreateNewInstanceUsingNoParamConstructor$thenJCUnitExceptionWillBeThrown() {
    ReflectionUtils.create(C3.class);
  }

  @SuppressWarnings("unused")
  public static class C4 extends C {
    public C4() throws Throwable {
      throw new RuntimeException();
    }
  }

  @Test(expected = RuntimeException.class)
  public void givenClassThrowsRuntimeException$whenPerformCreateNewInstanceUsingNoParamConstructor$thenJCUnitExceptionWillBeThrown() {
    ReflectionUtils.create(C4.class);
  }

  @SuppressWarnings("unused")
  public static class C5 extends C {
    public C5() throws Throwable {
      throw new IOException();
    }
  }

  @Test(expected = JCUnitException.class)
  public void givenClassThrowsCheckedException$whenPerformCreateNewInstanceUsingNoParamConstructor$thenJCUnitExceptionWillBeThrown() {
    ReflectionUtils.create(C5.class);
  }

  @Test(expected = JCUnitException.class)
  public void testGetDefaultValueOfAnnotation() {
    ReflectionUtils.getDefaultValueOfAnnotation(FactorField.class, "xyz");
  }

  @Test
  public void deepEq$null_null() {
    assertTrue(Utils.deepEq(null, null));
  }

  @Test
  public void deepEq$null_nonnull() {
    assertFalse(Utils.deepEq(null, ""));
  }

  @Test
  public void deepEq$nonnull_null() {
    assertFalse(Utils.deepEq("", null));
  }

  @Test
  public void deepEq$nonnull_nonnull$eq1() {
    assertTrue(Utils.deepEq("", ""));
  }

  @Test
  public void deepEq$nonnull_nonnull$eq2() {
    //noinspection RedundantStringConstructorCall
    assertTrue(Utils.deepEq(
        "",
        new String("")/* Create a string object intentionally for test*/
    ));
  }

  @Test
  public void deepEq$nonnull_nonnull$ne() {
    assertFalse(Utils.deepEq("", "-"));
  }

  @Test
  public void deepEq$arr_obj$ne() {
    assertFalse(Utils.deepEq(new Object[] { "" }, new Object()));
  }

  @Test
  public void deepEq$arr_nullj$ne() {
    assertFalse(Utils.deepEq(new Object[] { "" }, null));
  }

  @Test
  public void deepEq$null_arr$ne() {
    assertFalse(Utils.deepEq(null, new Object[] { "" }));
  }

  @Test
  public void deepEq$obj_arr$ne() {
    assertFalse(Utils.deepEq(new Object(), new Object[] { "" }));
  }

  @Test
  public void deepEq$arr_arr$eq() {
    assertTrue(Utils.deepEq(new Object[] { "" }, new Object[] { "" }));
  }

  @Test
  public void deepEq$arr_arr$ne1() {
    assertFalse(Utils.deepEq(new Object[] { "" }, new Object[] { "", "" }));
  }

  @Test
  public void deepEq$arr_arr$ne2() {
    assertFalse(Utils.deepEq(new Object[] { "", "" }, new Object[] { "" }));
  }

  @Test
  public void deepEq$arr_arr$ne3() {
    assertFalse(Utils.deepEq(
        new Object[] { "", "" },
        new Object[] { "", "*" }
    ));
  }

  @Test
  public void deepEq$nestedarr_nestedarr$eq() {
    assertTrue(Utils.deepEq(
        new Object[] { "", new Object[] { "" } },
        new Object[] { "", new Object[] { "" } }));
  }

  @Test
  public void deepEq$nestedarr_nestedarr$ne1() {
    assertFalse(Utils.deepEq(
        new Object[] { "", new String[] { "" } },
        new Object[] { "", new Object[] { "" } }));
  }

  @Test
  public void deepEq$nestedarr_nestedarr$ne2() {
    assertFalse(Utils.deepEq(
        new Object[] { "", new Object[] { "" } },
        new Object[] { "", new Object[] { "*" } }));
  }

  @Test
  public void deepEq$nestedarr_nestedarr$ne3() {
    assertFalse(Utils.deepEq(
        new Object[] { null, new Object[] { "" } },
        new Object[] { "", new Object[] { "" } }));
  }

  @Test
  public void deepEq$nestedarr_nestedarr$ne4() {
    assertFalse(Utils.deepEq(
        new Object[] { "", new Object[] { "" } },
        new Object[] { "", new Object[] { null } }));
  }

  @Test
  public void testFilter() {
    assertEquals(
        2,
        Utils.filter(Arrays.asList("Hello", "world", "!"),
            new Utils.Predicate<String>() {
              @Override
              public boolean apply(String in) {
                return !"world".equals(in);
              }
            }).size());
  }

  @Test
  public void testSingleton() {
    assertEquals(
        Arrays.asList("A", "B"),
        Utils.dedup(Arrays.asList("A", "A", "B")
        ));
  }
}

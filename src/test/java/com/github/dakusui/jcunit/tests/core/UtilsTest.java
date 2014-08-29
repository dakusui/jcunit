package com.github.dakusui.jcunit.tests.core;

import com.github.dakusui.jcunit.core.Utils;
import org.junit.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

public class UtilsTest {
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Ann1 {
  }

  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Ann2 {
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
    assertEquals("f", Utils.getField(testObj, "f", Ann1.class).getName());
    assertEquals("h", Utils.getField(testObj, "h", Ann1.class).getName());
  }

  @Test
  public void normalTestValidField2() {
    TestClass testObj = new TestClass();
    //noinspection unchecked
    assertEquals("h", Utils.getField(testObj, "h", Ann1.class, Ann2.class).getName());
  }

  @Test
  public void normalTestValidField3() {
    TestClass testObj = new TestClass();
    //noinspection unchecked
    assertEquals("i", Utils.getField(testObj, "i").getName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void abnormalTestUndefinedField() {
    TestClass testObj = new TestClass();
    //noinspection unchecked
    Utils.getField(testObj, "e", Ann1.class).getName();
  }

  @Test(expected = IllegalArgumentException.class)
  public void abnormalTestFieldNotAnnotated() {
    TestClass testObj = new TestClass();
    //noinspection unchecked
    Utils.getField(testObj, "i", Ann1.class).getName();
  }

  @Test
  public void abnormalTestFieldInsufficientAnnotations() {
    TestClass testObj = new TestClass();
    //noinspection unchecked
    Utils.getField(testObj, "f", Ann1.class, Ann2.class).getName();
  }

  public static class TestClass2 {
    private Object f;
  }

  @Test
  public void abnormalTestSetFieldValueToPrivateField() throws NoSuchFieldException {
    Field f = TestClass2.class.getDeclaredField("f");
    Utils.setFieldValue(new TestClass2(), f, "Helloworld");
  }

  @Test
  public void defaultFormatterTest() {
    assertEquals(null, Utils.Formatter.INSTANCE.format(null));
    assertEquals("hello", Utils.Formatter.INSTANCE.format("hello"));
  }
}

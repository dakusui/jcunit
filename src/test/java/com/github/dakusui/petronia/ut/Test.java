package com.github.dakusui.petronia.ut;

import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;
import java.lang.reflect.Method;

public class Test {
  public static void s(Object o) {
  }

  public static void r(String s) {
  }

  public static void q(Object o) {
  }

  public static void q(String s) {
  }

  public static void m(int i) {
    System.out.println("m(int)");
  }

  public static void m(int i, int j) {
    System.out.println("m(int,int)");
  }

  public static void m(int i, int j, String[] k) {
    System.out.println("m(int,int,String[])");
  }

  public static void m(int i, int j, short p1, long p2, char p3, boolean p4,
      float p5, boolean p7, String[] k, String l, Object m) {
    System.out.println("m(int,int,String[])");
  }

  public static void m(Integer i) {
    System.out.println("m(Integer)");
  }

  public static void n(int i) {
    System.out.println("n(int)");
  }

  public static void o(Integer i) {
    System.out.println("o(Integer)");
  }

  public static void main(String[] argse) throws ClassNotFoundException {
    /*
     * / System.out.println(Class.forName("[Ljava.lang.String;"));
     * System.out.println(Class.forName("[I"));
     * System.out.println(Class.forName("I"));
     * System.out.println(Class.forName("I").equals(Integer.TYPE)); /
     */

    System.out.println(getMethod("q", String.class));
    System.out.println(getMethod("q", Object.class));
    System.out.println(getMethod("q", Serializable.class));
    System.out.println(getMethod("r", String.class));
    System.out.println(getMethod("r", Object.class));
    System.out.println(getMethod("s", String.class));
    System.out.println(getMethod("s", Object.class));
  }

  public static void _main(String[] args) throws SecurityException,
      NoSuchMethodException {
    System.out.printf("m(Integer):%s\n",
        ArrayUtils.toString(getMethod("m", Integer.class)));
    System.out.printf("m(int,int):%s\n",
        ArrayUtils.toString(getMethod("m", Integer.TYPE, Integer.TYPE)));
    System.out.printf("m(int,int,String[]):%s\n", ArrayUtils
        .toString(getMethod("m", Integer.TYPE, Integer.TYPE,
            new String[] { }.getClass())));
    System.out.printf("m(int):%s\n",
        ArrayUtils.toString(getMethod("m", Integer.TYPE)));
    System.out.printf("m(Short):%s\n",
        ArrayUtils.toString(getMethod("m", Short.class)));
    System.out.printf("m(short):%s\n",
        ArrayUtils.toString(getMethod("m", Short.TYPE)));
    System.out.printf("n(Integer):%s\n",
        ArrayUtils.toString(getMethod("n", Integer.class)));
    System.out.printf("n(int):%s\n",
        ArrayUtils.toString(getMethod("n", Integer.TYPE)));
    System.out.printf("o(Integer):%s\n",
        ArrayUtils.toString(getMethod("o", Integer.class)));
    System.out.printf("o(int):%s\n",
        ArrayUtils.toString(getMethod("o", Integer.TYPE)));
    System.out.printf("p(Integer):%s\n",
        ArrayUtils.toString(getMethod("p", Integer.class)));
    System.out.printf("p(int):%s\n", getMethod("p", Integer.TYPE));
    // System.out.printf("p(null):%s\n", ArrayUtils.toString(getMethod("p",
    // null)));
    System.out.printf("p([null]):%s\n",
        ArrayUtils.toString(getMethod("p", new Class<?>[] { null })));

    System.out.println("==========");

    System.out.println("-- m(1) --");
    m(1);
    System.out.println("-- m(new Integer(1)) --");
    m(new Integer(1));
    System.out.println("-- n(1) --");
    n(1);
    System.out.println("-- n(new Integer(1)) --");
    n(new Integer(1));
    System.out.println("-- o(1) --");
    o(1);
    System.out.println("-- o(new Integer(1)) --");
    o(new Integer(1));

    System.out.println("-->" + new String[] { "hi" });
    System.out.println("-->" + new int[] { 1, 2, 3 });
    Object a = null;
    System.out.printf("--> ==> %s\n", a);
  }

  protected static String getMethod(String methodName, Class<?>... paramType) {
    try {
      Method m = Test.class.getDeclaredMethod(methodName,
          (Class<?>[]) paramType);
      return m.toGenericString();
    } catch (SecurityException e) {
      assert false;
    } catch (NoSuchMethodException e) {
      return null;
    }
    return null;
  }
}

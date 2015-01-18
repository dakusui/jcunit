package com.github.dakusui.jcunit.fsm;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public abstract class FSMBase<SUT> implements FSM<SUT> {
  public static interface TestBase<T> {
    public static final String STR = "";
    public TestBase<T> typed();
  }
  public static enum Test implements TestBase<String> {
    HELLO {
      public void test(String o) {
        System.out.println("overridden");
      }
    };
    public static final String STR   = "2";
    public static final Test   HI    = HELLO;
    public static final String test  = "";
    public              String test2 = "";

    public void test(String o) {
      System.out.println("original");
    }
    @Override
    public TestBase<String> typed() {
      return new TestBase<String>() {
        @Override
        public TestBase<String> typed() {
          return null;
        }
      };
    }
  }

  public FSMBase() {
  }

  protected void setStateEnum(Class<? extends Enum> stateEnum) {

  }

  protected void setActionEnum(Class<? extends Enum> actionEnum) {

  }

  public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
    for (Field each : Test.class.getFields()) {
      int m = each.getModifiers();
      System.out.println(String.format("name:%s public:%s static:%s final:%s enum const:%s type:%s declaring class:%s",
          each.getName(),
          Modifier.isPublic(m),
          Modifier.isStatic(m),
          Modifier.isFinal(m),
          each.isEnumConstant(),
          each.getType(),
          each.getDeclaringClass().getSimpleName()));
    }
    System.out.println("****");
    Method m = Test.class.getMethod("typed");
    System.out.println("----" + m.getGenericReturnType());
    System.out.println("----" + Arrays.toString(m.getReturnType().getGenericInterfaces()));
    System.out.println("----" + m.getReturnType().getGenericSuperclass());
    System.out.println(Test.class.getField("STR").get(null));
    m.invoke(Test.HELLO, new Object());
  }
}

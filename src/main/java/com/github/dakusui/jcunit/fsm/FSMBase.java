package com.github.dakusui.jcunit.fsm;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public abstract class FSMBase<SUT> implements FSM<SUT> {
  public static interface TestBase {
    public static final String STR = "";
  }
  public static enum Test implements TestBase {
    HELLO {
      public void test(String o) {
        System.out.println("overridden");
      }
    };
    public static final Test HI = HELLO;
    public static final String test = "";
    public String test2="";
    public void test(String o) {
      System.out.println("original");
    }
  }
  public FSMBase() {
  }

  protected void setStateEnum(Class<? extends Enum> stateEnum) {

  }

  protected void setActionEnum(Class<? extends Enum> actionEnum) {

  }

  public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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

    Method m = Test.class.getMethod("test", String.class);
    m.invoke(Test.HELLO, new Object());
  }
}

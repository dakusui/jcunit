package com.github.dakusui.jcunit.fsm;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public abstract class FSMBase<SUT> implements FSM<SUT> {
  public static enum Test {
    HELLO;
    public static final Test HI = HELLO;
    public static final String test = "";
    public String test2="";
  }
  public FSMBase() {
  }

  protected void setStateEnum(Class<? extends Enum> stateEnum) {

  }

  protected void setActionEnum(Class<? extends Enum> actionEnum) {

  }

  public static void main(String[] args) {
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
  }
}

package com.github.dakusui.jcunit.fsm.sut;

public class FlyingSpaghettiMonster {
  public String cook() {
    String msg = "cooking!";
    System.out.println(msg);
    return msg;
  }

  public void eat() {
    System.out.println("yummy!");
  }
}

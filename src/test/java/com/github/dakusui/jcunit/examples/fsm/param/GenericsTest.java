package com.github.dakusui.jcunit.examples.fsm.param;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 種類              |宣言|バインド|?|境界|&
 * 型変数の宣言       |◯   |×     |△*1|◯|◯
 * 型変数へのバインド  |×   |◯     |×|×|×
 * 変数の型の宣言     |×   |◯     |◯|◯|×
 * 型変数での変数宣言  |×   |×     |×|×|×
 */
public class GenericsTest {
  static class A {
    public String a() {
      return "a";
    }
  }
  static class B extends A {
    public String b() {
      return "b";
    }
  }
  static class BB extends A {
    public String bb() {
      return "bb";
    }
  }
  static class C extends B {
    public String c() {
      return "c";
    }
  }

  @Test
  public void test1() {
    List<A> origin = new ArrayList<A>();
    List<? super A> in = origin;
    in.add(new A());
    in.add(new B());
    in.add(new BB());
    in.add(new C());
    List<? extends A> out = origin;
    System.out.println(out.get(0).a());
    System.out.println(out.get(1).a());
    System.out.println(out.get(2).a());
    System.out.println(out.get(3).a());
  }

  @Test
  public void test2() {
    List<B> origin = new ArrayList<B>();
    List<? super B> in = origin;
    in.add(new B());
    in.add(new C());
    List<? extends B> out = origin;
    System.out.println(out.get(0).a());
    System.out.println(out.get(1).a());
  }

  @Test
  public void test3() {
    class DD {
      public void test() {
        System.out.println("dd");
      }
    };
    new DD().test();
  }


}

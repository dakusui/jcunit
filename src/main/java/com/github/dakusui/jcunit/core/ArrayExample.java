package com.github.dakusui.jcunit.core;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Created by hiroshi on 7/14/14.
 */
public class ArrayExample {
  public static void main(String[] args) {
    Object a = new int[1];
    Array.set(a, 0, new Integer(100));

    System.out.println(Arrays.toString((int[]) a));
  }
}

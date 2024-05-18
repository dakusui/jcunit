package com.github.dakusui.java8.template;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
public class Java8App {
  public static void main(String... args) {
    for (String each: args)
      System.out.println(each);
  }

  public String process(String s) {
    return "processed:<" + s + ">";
  }
}

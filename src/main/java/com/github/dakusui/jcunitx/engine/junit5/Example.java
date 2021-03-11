package com.github.dakusui.jcunitx.engine.junit5;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@ExtendWith(Example.CustomBeforeAllExtension.class)
public class Example {
  @BeforeAll
  @Retention(RUNTIME)
  @ExtendWith(CustomBeforeAllExtension.class)
  public @interface CustomBeforeAll {
  }

  public static class CustomBeforeAllExtension implements BeforeAllCallback {
    @Override
    public void beforeAll(ExtensionContext context) {
      System.out.println("beforeAll(ExtensionContext) is called");
    }
  }

  @BeforeAll
  public static void beforeAll() {
    System.out.println("beforeAll is called");
  }

  @CustomBeforeAll
  public static void customBeforeAll() {
    System.out.println("customBeforeAll is called");
  }

  @Test
  public void test() {
    System.out.println("test is called");
  }
}

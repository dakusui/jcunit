package com.github.dakusui.jcunit8.sandbox.example;

import com.github.jcunit.annotations.JCUnitTest;
import com.github.jcunit.annotations.Given;
import com.github.jcunit.annotations.Named;
import com.github.jcunit.runners.junit5.JCUnitTestEngine;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
@Disabled
@ExtendWith(JCUnitTestEngine.class)
public class TestExample3 {
  @BeforeAll
  public static void beforeAll() {
    System.out.println("beforeAll");
  }

  @BeforeEach
  public void beforeEach() {
    System.out.println("beforeEach");
  }

  @Named
  public static boolean precondition() {
    return true;
  }
  @Given("precondition")
  @JCUnitTest
  public void testMethod() {
    System.out.println("testMethod");
  }

  @AfterEach
  public void afterEach() {
    System.out.println("afterEach");
  }

  @AfterAll
  public static void afterAll() {
    System.out.println("afterAll");
  }
}

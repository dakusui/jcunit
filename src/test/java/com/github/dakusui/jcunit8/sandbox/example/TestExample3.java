package com.github.dakusui.jcunit8.sandbox.example;

import com.github.jcunit.annotations.JCUnitTest;
import com.github.jcunit.annotations.Given;
import org.junit.jupiter.api.*;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
@Disabled
public class TestExample3 {
  @BeforeAll
  public static void beforeAll() {
    System.out.println("beforeAll");
  }

  @BeforeEach
  public void beforeEach() {
    System.out.println("beforeEach");
  }

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

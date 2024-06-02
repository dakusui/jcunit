package com.github.dakusui.jcunit8.sandbox.example;

import com.github.jcunit.annotations.JCUnitTest;
import com.github.jcunit.runners.junit4.annotations.Given;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
public class TestExample3 {
  @BeforeAll
  public static void beforeAll() {
    System.out.println("beforeAll");
  }

  @BeforeEach
  public void beforeEach() {
    System.out.println("beforeEach");
  }

  @Given("")
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

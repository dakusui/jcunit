package com.github.dakusui.java8.template;

import com.github.dakusui.java8.template.testutils.TestBase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.github.valid8j.fluent.Expectations.assertStatement;
import static com.github.valid8j.fluent.Expectations.that;

public class Java8AppTest extends TestBase {
  @Test
  public void testMain() {
    Java8App.main("Hello", "world");
  }
  
  @Test
  public void givenAppObject_whenProcessHello_thenProcessedHello_step1() {
    String s = "hello";
    // Remember, `Expectations` is the class you start with to write your test using `valid8j`.
    // `assertStatement(Statement)` is the most basic way to begin with.
    assertStatement(
        // Since it accepts `Statement`, you try `Statement` class.
        // Since you are testing your class, not String, short, int, long., you are choosing `objectValue` method and give your object `new Java8App()`.
        that(new Java8App())
            // Invoke your method `process`, which takes a string parameter.
            // valid8j chooses appropriate method automatically (narrowest possible one.)
            .invoke("process", s)
            // Optional: Let the compiler know it returns string.
            .asString()
            // Let the object know you are now validating the returned value.
            .then()
            // Does it contain a string 'processed:'?
            .containing("processed:")
            // Does it contain `s`?
            .containing(s));
    // Even if you didn't do `asString()`, you can still do `isEqualTo("processed:" + s)`.
  }
  
  @Test
  public void givenAppObject_whenProcessHello_thenProcessedHello_step2() {
    String s = "hello";
    // Let's clean up by using static import.
    assertStatement(that(new Java8App())
        .invoke("process", s)
        .asString()
        .then()
        .containing("processed:")
        .containing(s));
  }
  
  /**
   * //@formatter:off
   * This is an example to show how a failure message from thincrest-pcond looks like.
   * "expected" and "actual" will be displayed side-by-side by your IDE.
   *
   * .expected
   * ----
   *     Java8App@3eb7fc54->transform                         ->"processed:hello"
   *                      ->  <>.process(<hello>)             ->"processed:hello"
   *     "processed:hello"->  castTo[String]                  ->"processed:hello"
   *                      ->THEN:allOf                        ->true
   * [0]                  ->    containsString[notProcessed:] ->true
   *                      ->    containsString[hello]         ->true
   *
   * .Detail of failure [0]
   * ---
   * containsString[notProcessed:]
   * ---
   * ----
   *
   * .actual
   * ----
   *     Java8App@3eb7fc54->transform                         ->"processed:hello"
   *                      ->  <>.process(<hello>)             ->"processed:hello"
   *     "processed:hello"->  castTo[String]                  ->"processed:hello"
   *                      ->THEN:allOf                        ->false
   * [0]                  ->    containsString[notProcessed:] ->false
   *                      ->    containsString[hello]         ->true
   *
   * .Detail of failure [0]
   * ---
   * processed:hello
   * ---
   * ----
   *
   * Which part of your expectation isn't met will be shown with the actual value.
   * //@formatter:on
   */
  @Disabled
  @Test
  public void givenAppObject_whenProcessHello_thenNotProcessedHello() {
    String s = "hello";
    assertStatement(that(new Java8App())
        .invoke("process", s)
        .asString()
        .then()
        .containing("notProcessed:")
        .containing(s));
  }
}

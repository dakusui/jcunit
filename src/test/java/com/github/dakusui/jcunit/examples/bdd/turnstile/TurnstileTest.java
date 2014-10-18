package com.github.dakusui.jcunit.examples.bdd.turnstile;

import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.examples.bdd.Given;
import com.github.dakusui.jcunit.examples.bdd.Then;
import org.junit.Test;

public class TurnstileTest {
  Turnstile sut = new Turnstile();

  public void turnstileIsLocked() {
    sut.setState(Turnstile.State.locked);
  }

  public void turnstileIsUnlocked() {
    sut.setState(Turnstile.State.unlocked);
  }

  @Given("turnstileIsLocked")
  @When("coin")
  @Then({"unlock"})
  @Test
  public void test1() {
  }

  @Given("turnstileIsLocked")
  @When("pass")
  @Then({"alarm"})
  @Test
  public void test2() {

  }

  @Given("turnstileIsUnlocked")
  @When("coin")
  @Then({"thankyou"})
  @Test
  public void test3() {

  }


  @Given("turnstileIsUnlocked")
  @When("pass")
  @Then({"lock"})
  @Test
  public void test4() {

  }
}

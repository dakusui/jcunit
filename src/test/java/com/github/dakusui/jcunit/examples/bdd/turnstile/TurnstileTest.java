package com.github.dakusui.jcunit.examples.bdd.turnstile;

import com.github.dakusui.jcunit.core.Param;
import com.github.dakusui.jcunit.core.When;
import com.github.dakusui.jcunit.examples.bdd.JCBehavior;
import com.github.dakusui.jcunit.examples.dfsm.Turnstile;
import org.junit.Test;

public class TurnstileTest {
  Turnstile sut = new Turnstile();

  public void turnstileIsLocked() {
    sut.setState(Turnstile.State.locked);
  }

  public void turnstileIsUnlocked() {
    sut.setState(Turnstile.State.unlocked);
  }

  @JCBehavior.Given(@Param("turnstileIsLocked"))
  @When("coin")
  @JCBehavior.Then(@Param("unlock"))
  @Test
  public void test1() {
  }

  @JCBehavior.Given(@Param("turnstileIsLocked"))
  @When("pass")
  @JCBehavior.Then(@Param("alarm"))
  @Test
  public void test2() {

  }

  @JCBehavior.Given(@Param("turnstileIsUnlocked"))
  @When("coin")
  @JCBehavior.Then(@Param("thankyou"))
  @Test
  public void test3() {

  }


  @JCBehavior.Given(@Param("turnstileIsUnlocked"))
  @When("pass")
  @JCBehavior.Then(@Param("lock"))
  @Test
  public void test4() {

  }
}

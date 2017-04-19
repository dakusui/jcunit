package com.github.dakusui.jcunit8.ut.examples;

import com.github.dakusui.jcunit8.examples.bankaccount.BankAccountExample;
import com.github.dakusui.jcunit8.examples.flyingspaghettimonster.FlyingSpaghettiMonsterExample;
import com.github.dakusui.jcunit8.examples.quadraticequation.QuadraticEquationExample;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class ExamplesTest {
  @Test
  public void quadraticEquationSolver() {
    Result result = JUnitCore.runClasses(QuadraticEquationExample.class);
    System.out.println(result);
  }

  @Test
  public void flyingSpaghettiMonster() {
    Result result = JUnitCore.runClasses(FlyingSpaghettiMonsterExample.class);
    System.out.println(result);
  }

  @Test
  public void bankAccount() {
    Result result = JUnitCore.runClasses(BankAccountExample.class);
    System.out.println(result);
  }
}

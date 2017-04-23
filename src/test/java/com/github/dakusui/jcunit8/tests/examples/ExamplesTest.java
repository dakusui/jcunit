package com.github.dakusui.jcunit8.tests.examples;

import com.github.dakusui.jcunit8.examples.bankaccount.BankAccountExample;
import com.github.dakusui.jcunit8.examples.flyingspaghettimonster.FlyingSpaghettiMonsterExample;
import com.github.dakusui.jcunit8.examples.quadraticequation.QuadraticEquationExample;
import com.github.dakusui.jcunit8.testutils.ResultUtils;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import static com.github.dakusui.jcunit8.testutils.UTBase.matcher;

public class ExamplesTest {
  @Test
  public void quadraticEquationSolver() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(QuadraticEquationExample.class),
        matcher(
            Result::wasSuccessful
        )
    );
  }

  @Test
  public void flyingSpaghettiMonster() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(FlyingSpaghettiMonsterExample.class),
        matcher(
            Result::wasSuccessful
        )
    );
  }

  @Test
  public void bankAccount() {
    ResultUtils.validateJUnitResult(
        JUnitCore.runClasses(BankAccountExample.class),
        matcher(
            Result::wasSuccessful
        )
    );
  }
}

package com.github.dakusui.jcunit.examples.bank;

import com.github.dakusui.jcunit.plugins.caengines.IpoGcCoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.constraints.SmartConstraintCheckerImpl;
import com.github.dakusui.jcunit.regex.RegexLevelsProvider;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.*;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;


@RunWith(JCUnit.class)
@GenerateCoveringArrayWith(
    engine = @Generator(value = IpoGcCoveringArrayEngine.class),
    checker = @Checker(value = SmartConstraintCheckerImpl.NoNegativeTests.class)
)
public class BankAccountExample {
  @SuppressWarnings("WeakerAccess")
  @FactorField(intLevels = { 100, 200, 500 })
  public int depositAmount;
  @FactorField(intLevels = { 100, 200, 500 })
  @SuppressWarnings("WeakerAccess")
  public int withdrawAmount;
  @FactorField(intLevels = { 100, 200, 500 })
  @SuppressWarnings("WeakerAccess")
  public int transferAmount;

  @SuppressWarnings("WeakerAccess")
  @FactorField(
      levelsProvider = RegexLevelsProvider.class,
      args = { @Value("open(deposit|withdraw|transfer){0,2}getBalance") }
  )
  public List<String> operationSequence;


  private Account account;

  @Condition
  public boolean overdraftHappens() {
    return calculateExpectedBalance() < 0;
  }

  @Given("!overdraftHappens")
  @Test
  public void whenPerformScenario$thenBalanceIsCorrect() {
    assertEquals(calculateExpectedBalance(), performSequence());
  }

  @Given("overdraftHappens")
  @Test(expected = InsufficientBalanceException.class)
  public void whenPerformSequence$thenInsufficientBalanceWillBeThrown() {
    performSequence();
  }

  private int performSequence() {
    for (String operation : operationSequence) {
      if ("open".equals(operation)) {
        this.account = Account.open();
      } else if ("deposit".equals(operation)) {
        this.account.deposit(this.depositAmount);
      } else if ("withdraw".equals(operation)) {
        this.account.withdraw(this.withdrawAmount);
      } else if ("transfer".equals(operation)) {
        this.account.transfer(this.transferAmount);
      } else if ("getBalance".equals(operation)) {
        return this.account.getBalance();
      }
    }
    throw new RuntimeException();
  }

  private int calculateExpectedBalance() {
    int ret = 0;
    for (String operation : operationSequence) {
      if ("open".equals(operation)) {
        this.account = Account.open();
      } else if ("deposit".equals(operation)) {
        ret += this.depositAmount;
      } else if ("withdraw".equals(operation)) {
        ret -= this.withdrawAmount;
      } else if ("transfer".equals(operation)) {
        ret -= this.transferAmount;
      } else if ("getBalance".equals(operation)) {
        return ret;
      }
      if (ret < 0)
        return -1;
    }
    throw new RuntimeException();
  }

  static class Account {

    int balance = 0;

    static Account open() {
      return new Account();
    }

    void deposit(int amount) {
      this.balance += amount;
    }

    void withdraw(int amount) {
      if (amount > balance)
        throw new InsufficientBalanceException();
      this.balance -= amount;
    }

    void transfer(int amount) {
      if (amount > balance)
        throw new InsufficientBalanceException();
      this.balance -= amount;
    }

    int getBalance() {
      return this.balance;
    }
  }

  static class InsufficientBalanceException extends RuntimeException {
  }

}

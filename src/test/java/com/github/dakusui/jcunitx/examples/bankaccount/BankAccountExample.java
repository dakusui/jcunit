package com.github.dakusui.jcunitx.examples.bankaccount;

import com.github.dakusui.jcunit8.examples.bankaccount.BankAccount;
import com.github.dakusui.jcunit8.models.scenario.ActionSequence;
import com.github.dakusui.jcunit8.models.scenario.ParameterizedAction;
import com.github.dakusui.jcunit8.models.scenario.Scenario;
import com.github.dakusui.jcunit8.testutils.ParameterUtils;
import com.github.dakusui.jcunitx.runners.annotations.*;
import com.github.dakusui.jcunitx.runners.junit5.JCUnitX;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.github.dakusui.jcunit8.testutils.ParameterUtils.simple;
import static com.github.dakusui.pcond.TestAssertions.assertThat;
import static com.github.dakusui.pcond.functions.Predicates.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(JCUnitX.class)
@Constraints({ "withdrawUsed" })
public class BankAccountExample {
  private final BankAccount anotherAccount = BankAccount.open();

  /**
   * A method to calculate expected balance based on the specification, not by the SUT.
   *
   * @param scenario A scenario to be performed
   * @return Expected balance.
   */
  private static int calculateExpectedBalance(ActionSequence<BankAccountDriver> scenario) {
    int balance = 0;
    for (ParameterizedAction op : scenario) {
      int prev = balance;
      if ("deposit".equals(op.name()))
        balance += op.<Integer>arg(0);
      else if ("withdraw".equals(op.name()))
        balance -= op.<Integer>arg(0);
      else if ("transfer".equals(op.name()))
        balance -= op.<Integer>arg(0);

      if (balance < 0)
        return prev;
    }
    return balance;
  }

  @ParameterSource
  public Scenario.Factory scenario() {
    return ParameterUtils.scenario("open  deposit (deposit|withdraw|transfer){0,2} getBalance")
        .addParameter("deposit", simple(100, 200, 300, 400, 500, 600, -1))
        .addParameter("withdraw", simple(100, 200, 300, 400, 500, 600, -1))
        .addParameter("transfer", simple(100, 200, 300, 400, 500, 600, -1));
  }

  @Condition
  public boolean depositUsed(@From("scenario") ActionSequence<BankAccountDriver> scenario) {
    return scenario.stream()
        .filter(each -> each.name().equals("deposit"))
        .anyMatch(each -> each.<Integer>arg(0) > 0);
  }

  @Condition
  public boolean withdrawUsed(@From("scenario") ActionSequence<BankAccountDriver> scenario) {
    return scenario.stream()
        .filter(each -> each.name().equals("withdraw"))
        .anyMatch(each -> each.<Integer>arg(0) > 0);
  }

  @Condition
  public boolean transferUsed(@From("scenario") ActionSequence<BankAccountDriver> scenario) {
    return scenario.stream()
        .filter(each -> each.name().equals("transfer"))
        .anyMatch(each -> each.<Integer>arg(0) > 0);
  }

  @Condition
  public boolean overdraftHappens(@From("scenario") ActionSequence<BankAccountDriver> scenario) {
    return calculateExpectedBalance(scenario) < 0;
  }

  @Test
  @Given("!overdraftHappens")
  public void whenPerformScenario$thenBalanceIsCorrect(@From("scenario") ActionSequence<BankAccountDriver> scenario) {
    int balance = scenario.perform().lastValue();
    assertThat(
        balance,
        equalTo(calculateExpectedBalance(scenario)));
  }

  @Test
  @Given("overdraftHappens")
  public void whenPerformScenario$thenExceptionThrown(@From("scenario") ActionSequence<BankAccountDriver> scenario) {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          try {
            scenario.perform().lastValue();
          } catch (IllegalArgumentException e) {
            assertThat(
                scenario.driverObject().getBalance(),
                equalTo(calculateExpectedBalance(scenario)));
          }
        });
  }

  @PerformScenarioWith("scenario")
  public BankAccountDriver createDriverObject() {
    return new BankAccountDriver(this.anotherAccount);
  }

  public static class BankAccountDriver {
    private final BankAccount anotherAccount;
    private       BankAccount myAccount;

    public BankAccountDriver(BankAccount anotherAccount) {
      this.myAccount = null;
      this.anotherAccount = anotherAccount;
    }

    @ForAction("open")
    public void open() {
      this.myAccount = BankAccount.open();
    }

    @ForAction("deposit")
    public void deposit(@From(".0") int amount) {
      this.myAccount.deposit(amount);
    }

    @ForAction("withdraw")
    public void withdraw(@From(".0") int amount) {
      this.myAccount.withdraw(amount);
    }

    @ForAction("transfer")
    public void transfer(@From(".0") int amount) {
      this.myAccount.transferTo(this.anotherAccount, amount);
    }

    @ForAction("getBalance")
    public int getBalance() {
      return this.myAccount.getBalance();
    }
  }
}

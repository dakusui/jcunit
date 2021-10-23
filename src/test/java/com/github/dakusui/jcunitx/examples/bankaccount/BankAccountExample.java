package com.github.dakusui.jcunitx.examples.bankaccount;

import com.github.dakusui.actionunit.actions.Named;
import com.github.dakusui.jcunit8.examples.bankaccount.BankAccount;
import com.github.dakusui.jcunit8.models.scenario.ActionSequence;
import com.github.dakusui.jcunit8.models.scenario.ParameterizedAction;
import com.github.dakusui.jcunit8.models.scenario.Scenario;
import com.github.dakusui.jcunit8.testutils.ParameterUtils;
import com.github.dakusui.jcunitx.runners.annotations.*;
import com.github.dakusui.jcunitx.runners.junit5.JCUnitX;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static com.github.dakusui.jcunit8.testutils.ParameterUtils.simple;
import static com.github.dakusui.pcond.TestAssertions.assertThat;
import static com.github.dakusui.pcond.functions.Predicates.equalTo;

@ExtendWith(JCUnitX.class)
@Constraints({ "depositUsed", "withdrawUsed" })
public class BankAccountExample {

  private final BankAccount anotherAccount = BankAccount.open();
  private       BankAccount myAccount;

  private static int calculateExpectedBalance(ActionSequence scenario) {
    int balance = 0;
    for (ParameterizedAction op : scenario) {
      if ("deposit".equals(op.name()))
        balance += op.<Integer>arg(0);
      else if ("withdraw".equals(op.name()))
        balance -= op.<Integer>arg(0);
      else if ("transfer".equals(op.name()))
        balance -= op.<Integer>arg(0);

      if (balance < 0)
        return balance;
    }
    return balance;
  }

  @ParameterSource
  public Scenario.Factory scenario() {
    return ParameterUtils.scenario("open  deposit(deposit|withdraw|transfer){0,2} getBalance")
        .addParameter("deposit", simple(100, 200, 300, 400, 500, 600, -1))
        .addParameter("withdraw", simple(100, 200, 300, 400, 500, 600, -1))
        .addParameter("transfer", simple(100, 200, 300, 400, 500, 600, -1));
  }

  public boolean depositUsed(
      @From("scenario") List<String> scenario,
      @From("depositAmount") int amount
  ) {
    //noinspection SimplifiableIfStatement
    if (!scenario.contains("deposit")) {
      return amount == -1;
    } else {
      return amount != -1;
    }
  }

  @Condition
  public boolean withdrawUsed(
      @From("scenario") List<String> scenario,
      @From("withdrawAmount") int amount
  ) {
    //noinspection SimplifiableIfStatement
    if (!scenario.contains("withdraw")) {
      return amount == -1;
    } else {
      return amount != -1;
    }
  }

  @Condition
  public boolean transferUsed(
      @From("scenario") ActionSequence scenario
  ) {
    return scenario.stream()
        .map(Named::name)
        .anyMatch(each -> each.equals("transfer"));
  }

  @Condition
  public boolean overdraftNotHappens(
      @From("scenario") ActionSequence scenario
  ) {
    return calculateExpectedBalance(scenario) >= 0;
  }

  @Test
  @Given("overdraftNotHappens")
  public void whenPerformScenario$thenBalanceIsCorrect(@From("scenario") ActionSequence scenario) {
    int balance = -1;
    scenario.perform();
    //for (String operation : scenario) {
    // balance = perform(operation, amountOfDeposit, amountOfWithdraw, amountOfTransfer);
    //}
    assertThat(
        balance,
        equalTo(calculateExpectedBalance(scenario)));
  }

  @Perform("open")
  public void open() {
    this.myAccount = BankAccount.open();
  }

  @Perform("deposit")
  public void deposit(@From(".0") int amount) {
    this.myAccount.deposit(amount);
  }

  @Perform
  public void withdraw(@From(".0") int amount) {
    this.myAccount.withdraw(amount);
  }

  @Perform
  public void transfer(@From(".0") int amount) {
    this.myAccount.transferTo(this.anotherAccount, amount);
  }
}

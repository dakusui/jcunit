package com.github.dakusui.jcunitx.examples.bankaccount2;

import com.github.dakusui.jcunitx.metamodel.parameters.EnhancedRegexParameter;
import com.github.dakusui.jcunitx.metamodel.parameters.SimpleParameter;
import com.github.dakusui.jcunitx.runners.helpers.ParameterUtils;
import com.github.dakusui.jcunitx.runners.junit4.JCUnit8;
import com.github.dakusui.jcunitx.runners.junit4.annotations.Condition;
import com.github.dakusui.jcunitx.runners.junit4.annotations.From;
import com.github.dakusui.jcunitx.runners.junit4.annotations.Given;
import com.github.dakusui.jcunitx.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.github.dakusui.jcunitx.metamodel.parameters.EnhancedRegexParameter.immediateValues;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

@RunWith(JCUnit8.class)
public class BankAccountExample2 {

  private final BankAccount2 anotherAccount = BankAccount2.open();
  private       BankAccount2 myAccount;

  private static int calculateBalance(List<String> scenario,
      int amountOfDeposit,
      int amountOfWithdraw,
      int amountOfTransfer) {
    int balance = 0;
    for (String op : scenario) {
      if ("deposit".equals(op)) {
        balance += amountOfDeposit;
      } else if ("withdraw".equals(op)) {
        balance -= amountOfWithdraw;
      } else if ("transfer".equals(op)) {
        balance -= amountOfTransfer;
      }
      if (balance < 0) {
        return balance;
      }
    }
    return balance;
  }

  @ParameterSource
  public EnhancedRegexParameter.Descriptor scenario() {
    return EnhancedRegexParameter.Descriptor.of("open deposit(deposit|withdraw|transfer){0,2}getBalance")
        .describe("open", singletonList(ParameterUtils.simple(immediateValues("Steve Smith", "Scot Tiger")).create("")))
        .describe("deposit", singletonList(ParameterUtils.simple(immediateValues(0, 1, 1_000_000)).create("")));

  }

  @ParameterSource
  public SimpleParameter.Descriptor<Integer> depositAmount() {
    return SimpleParameter.Descriptor.of(asList(100, 200, 300, 400, 500, 600, -1));
  }

  @ParameterSource
  public SimpleParameter.Descriptor<Integer> withdrawAmount() {
    return SimpleParameter.Descriptor.of(asList(100, 200, 300, 400, 500, 600, -1));
  }

  @ParameterSource
  public SimpleParameter.Descriptor<Integer> transferAmount() {
    return SimpleParameter.Descriptor.of(asList(100, 200, 300, 400, 500, 600, -1));
  }

  @Condition(constraint = true)
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

  @Condition(constraint = true)
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

  @Condition(constraint = true)
  public boolean transferUsed(
      @From("scenario") List<String> scenario,
      @From("transferAmount") int amount
  ) {
    //noinspection SimplifiableIfStatement
    if (!scenario.contains("transfer")) {
      return amount == -1;
    } else {
      return amount != -1;
    }
  }

  @Condition(constraint = true)
  public boolean overdraftNotHappens(
      @From("scenario") List<String> scenario,
      @From("depositAmount") int amountOfDeposit,
      @From("withdrawAmount") int amountOfWithdraw,
      @From("transferAmount") int amountOfTransfer
  ) {
    return calculateBalance(scenario, amountOfDeposit, amountOfWithdraw, amountOfTransfer) >= 0;
  }

  @Test
  @Given("overdraftNotHappens")
  public void whenPerformScenario$thenBalanceIsCorrect(
      @From("scenario") List<String> scenario,
      @From("depositAmount") int amountOfDeposit,
      @From("withdrawAmount") int amountOfWithdraw,
      @From("transferAmount") int amountOfTransfer
  ) {
    int balance = -1;
    for (String operation : scenario) {
      balance = perform(operation, amountOfDeposit, amountOfWithdraw, amountOfTransfer);
    }
    assertEquals(calculateBalance(scenario, amountOfDeposit, amountOfWithdraw, amountOfTransfer), balance);
  }

  @Test
  @Given("overdraftNotHappens")
  public void printScenario(
      @From("scenario") List<String> scenario,
      @From("depositAmount") int amountOfDeposit,
      @From("withdrawAmount") int amountOfWithdraw,
      @From("transferAmount") int amountOfTransfer
  ) {
    System.out.println(scenario + ":" + amountOfDeposit + ":" + amountOfWithdraw + ":" + amountOfTransfer);
  }

  private int perform(
      String operation,
      int amountOfDeposit,
      int amountOfWithdraw,
      int amountOfTransfer
  ) {
    int ret = -1;
    switch (operation) {
    case "open":
      myAccount = BankAccount2.open();
      break;
    case "deposit":
      myAccount.deposit(amountOfDeposit);
      break;
    case "withdraw":
      myAccount.withdraw(amountOfWithdraw);
      break;
    case "transfer":
      myAccount.transferTo(anotherAccount, amountOfTransfer);
      break;
    case "getBalance":
      ret = myAccount.getBalance();
      break;
    default:
      throw new AssertionError();
    }
    return ret;
  }
}

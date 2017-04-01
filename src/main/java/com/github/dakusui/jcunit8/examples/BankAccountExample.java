package com.github.dakusui.jcunit8.examples;

import com.github.dakusui.jcunit.runners.standard.annotations.Condition;
import com.github.dakusui.jcunit8.factorspace.Parameter.Regex;
import com.github.dakusui.jcunit8.factorspace.Parameter.Simple;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ConfigureWith;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ConfigureWith.ConfigFactory;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.Given;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.List;

import static java.util.Arrays.asList;

@RunWith(JCUnit8.class)
@ConfigureWith(BankAccountExample.BankAccountConfigFactory.class)
public class BankAccountExample {
  public static class BankAccountConfigFactory extends ConfigFactory.Impl {
    @SuppressWarnings("unused")
    @ParameterSource
    public static Regex.Factory<String> scenario() {
      return Regex.Factory.of("open(deposit|withdraw|transfer){0,2}getBalance");
    }

    @ParameterSource
    public static Simple.Factory<Integer> depositAmount() {
      return Simple.Factory.of(asList(100, 200, 300));
    }

    @ParameterSource
    public static Simple.Factory<Integer> withdrawAmount() {
      return Simple.Factory.of(asList(100, 200, 300));
    }

    @ParameterSource
    public static Simple.Factory<Integer> transferAmount() {
      return Simple.Factory.of(asList(100, 200, 300));
    }

    @Condition(constraint = true)
    public static boolean overdraftHappens(
        @From("scenario") List<String> scenario,
        @From("depositAmount") int amountOfDeposit,
        @From("withdrawAmount") int amountOfWithdraw,
        @From("transferAmount") int amountOfTransfer
    ) {
      return true;
    }
  }

  @SuppressWarnings("unused")
  @Theory
  @Given("!overdraftHappens")
  public void performScenario(
      @From("scenario") List<String> scenario
  ) {
  }
}

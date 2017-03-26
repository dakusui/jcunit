package com.github.dakusui.jcunit8.examples;

import com.github.dakusui.jcunit.runners.standard.annotations.Condition;
import com.github.dakusui.jcunit.runners.standard.annotations.Given;
import com.github.dakusui.jcunit8.factorspace.Parameter.Regex;
import com.github.dakusui.jcunit8.pipeline.Config;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ConfigureWith;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(JCUnit8.class)
@ConfigureWith(Config.Impl.class)
public class BankAccountExample {
  @ParameterSource
  public Regex.Factory<String> scenario() {
    return Regex.Factory.of("open(deposit|withdraw|transfer){0,2}getBalance") ;
  }

  @Test
  @Given("!overdraftHappens")
  public void performScenario(
      @From("scenario") List<String> scenario
  ) {

  }

  @Condition
  public boolean overdraftHappens(
      @From("scenario") List<String> scenario,
      @From("deposit") int amountOfDeposit,
      @From("withdraw") int amountOfWithdraw,
      @From("transfer") int amountOfTransfer
  ) {
    return true;
  }
}

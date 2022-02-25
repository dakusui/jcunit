package com.github.dakusui.jcunitx.examples.seed;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.examples.bankaccount.BankAccountExample;
import com.github.dakusui.jcunitx.pipeline.Requirement;
import com.github.dakusui.jcunitx.pipeline.stages.ConfigFactory;
import com.github.dakusui.jcunitx.runners.junit4.annotations.ConfigureWith;

@ConfigureWith(BankAccountExampleWithSeeds.Config.class)
public class BankAccountExampleWithSeeds extends BankAccountExample {
  public static class Config extends ConfigFactory.Base {
    @Override
    protected Requirement defineRequirement(Requirement.Builder defaultValues) {
      return defaultValues.withNegativeTestGeneration(
          false
      ).addSeed(
         AArray.builder().putRegex(
              "scenario", "open", "deposit", "withdraw", "getBalance"
          ).put(
              "depositAmount", 300
          ).put(
              "withdrawAmount", 200
          ).put(
              "transferAmount", -1
          ).build()
      ).build();
    }
  }
}

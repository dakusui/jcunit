package com.github.dakusui.jcunit8.examples.seed;

import com.github.dakusui.jcunit8.examples.bankaccount.BankAccountExample;
import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.pipeline.Requirement;
import com.github.jcunit.pipeline.stages.ConfigFactory;
import com.github.jcunit.runners.junit4.annotations.ConfigureWith;

@ConfigureWith(BankAccountExampleWithSeeds.Config.class)
public class BankAccountExampleWithSeeds extends BankAccountExample {
  public static class Config extends ConfigFactory.Base {
    @Override
    protected Requirement defineRequirement(Requirement.Builder defaultValues) {
      return defaultValues.withNegativeTestGeneration(false)
                          .addSeed(Tuple.builder()
                                        .putRegex("scenario", "open", "deposit", "withdraw", "getBalance")
                                        .put("depositAmount", 300)
                                        .put("withdrawAmount", 200)
                                        .put("transferAmount", -1)
                                        .build())
                          .build();
    }
  }
}

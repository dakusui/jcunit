package com.github.dakusui.jcunit8.examples.seed;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.examples.bankaccount.BankAccountExample;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.ConfigFactory;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ConfigureWith;

import static java.util.Arrays.asList;

@ConfigureWith(BankAccountExampleWithSeeds.Config.class)
public class BankAccountExampleWithSeeds extends BankAccountExample {
  public static class Config extends ConfigFactory.Base {
    @Override
    protected Requirement.Builder defineRequirement(Requirement.Builder builder) {
      return builder.withNegativeTestGeneration(
          false
      ).addSeed(
          new Tuple.Builder().put(
              "scenario", asList("open", "deposit", "withdraw", "getBalance")
          ).put(
              "depositAmount", 300
          ).put(
              "withdrawAmount", 200
          ).build()
      );
    }
  }
}

package com.github.dakusui.jcunit8.examples.seed;

//@ConfigureWith(BankAccountExampleWithSeeds.Config.class)
public class BankAccountExampleWithSeeds {
  /*
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

   */
}

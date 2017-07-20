package com.github.dakusui.jcunit8.examples.seed;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.examples.bankaccount.BankAccountExample;
import com.github.dakusui.jcunit8.runners.junit4.annotations.SeedSource;

import static java.util.Arrays.asList;

public class BankAccountExampleWithSeeds extends BankAccountExample {
  @SeedSource
  public Tuple standardTransaction() {
    return new Tuple.Builder(
    ).put(
        "scenario", asList("open", "deposit", "withdraw", "getBalance")
    ).put(
        "depositAmount", 300
    ).put(
        "withdrawAmount", 200
    ).build();
  }
}

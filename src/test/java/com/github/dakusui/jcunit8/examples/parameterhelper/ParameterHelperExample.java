package com.github.dakusui.jcunit8.examples.parameterhelper;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.examples.flyingspaghettimonster.FlyingSpaghettiMonster;
import com.github.dakusui.jcunit8.examples.flyingspaghettimonster.FlyingSpaghettiMonsterSpec;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.factorspace.fsm.Scenario;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.github.dakusui.jcunit8.runners.helpers.ParameterUtils.*;

/**
 * By using {@code Parameters.simple}, {@code regex}, or {@code fsm} methods,
 * you can make your take class look a bit cleaner.
 */
@RunWith(JCUnit8.class)
public class ParameterHelperExample {
  @ParameterSource
  public Parameter.Regex.Factory<String> scenario() {
    return regex("open deposit(deposit|withdraw|transfer){0,2}getBalance");
  }

  @ParameterSource
  public Parameter.Simple.Factory<Integer> depositAmount() {
    return simple(100, 200, 300, 400, 500, 600, -1);
  }

  @ParameterSource
  public Parameter.Simple.Factory<Integer> withdrawAmount() {
    return simple(100, 200, 300, 400, 500, 600, -1);
  }

  @ParameterSource
  public Parameter.Simple.Factory<Integer> transferAmount() {
    return simple(100, 200, 300, 400, 500, 600, -1);
  }

  @ParameterSource
  public Parameter.Factory flyingSpaghettiMonster() {
    return fsm(FlyingSpaghettiMonsterSpec.class, 1);
  }

  @ParameterSource
  public Parameter.Factory group() {
    return grouped(
    ).factor(
        "g1", "h", "i", "j", "A"
    ).factor(
        "g2", "k", "l", "m"
    ).factor(
        "g3", "n", "o", "p"
    ).constraint(
        "g1==A",
        tuple -> !tuple.get("g1").equals("A"),
        "g1"
    ).strength(
        2
    ).build();
  }

  @ParameterSource
  public Parameter.Factory seq() {
    return sequence("gallia", "est", "omnis", "divisa").withRepetition().size(4).build();
  }

  @Test
  public void printScenario(
      @From("scenario") List<String> bankAccountScenario,
      @From("depositAmount") int amountOfDeposit,
      @From("withdrawAmount") int amountOfWithdraw,
      @From("transferAmount") int amountOfTransfer,
      @From("flyingSpaghettiMonster") Scenario<FlyingSpaghettiMonster> fsmScenario,
      @From("seq") List<String> seq,
      @From("group") Tuple groupedFactor
  ) {
    System.out.println(bankAccountScenario + ":" + amountOfDeposit + ":" + amountOfWithdraw + ":" + amountOfTransfer + ":" + fsmScenario + ":" + seq + ":" + groupedFactor);
  }
}

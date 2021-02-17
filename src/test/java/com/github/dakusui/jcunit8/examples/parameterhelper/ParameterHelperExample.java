package com.github.dakusui.jcunit8.examples.parameterhelper;

import com.github.dakusui.jcunitx.annotations.From;
import com.github.dakusui.jcunitx.annotations.ParameterSource;
import com.github.dakusui.jcunitx.core.tuples.Tuple;
import com.github.dakusui.jcunitx.engine.junit4.JCUnit8;
import com.github.dakusui.jcunitx.model.parameter.Parameter;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;

import static com.github.dakusui.jcunitx.engine.helpers.ParameterUtils.*;

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
  public Parameter.Factory<Tuple> group() {
    return grouped()
        .factor("g1", "h", "i", "j", "A")
        .factor("g2", "k", "l", "m")
        .factor("g3", "n", "o", "p")
        .constraint("g1==A", tuple -> !tuple.get("g1").equals("A"), "g1")
        .strength(2)
        .build();
  }

  @ParameterSource
  public Parameter.Factory<List<String>> seq() {
    return sequence("gallia", "est", "omnis", "divisa")
        .withRepetition()
        .size(4)
        .build();
  }

  @Test
  public void test(@From("scenario") List<String> scenario, @From("transferAmount") int transferAmount, @From("depositAmount") int depositAmount, @From("withdrawAmount") int withdrawAmount, @From("group") Map<?, ?> group, @From("seq") List<String> seq) {
    System.out.print("scenario=" + scenario);
    System.out.print(" transferAmount=" + transferAmount);
    System.out.print(" depositAmount=" + depositAmount);
    System.out.print(" withdrawAmount=" + withdrawAmount);
    System.out.print(" group=" + group);
    System.out.print(" seq=" + seq);
    System.out.println();
  }
}

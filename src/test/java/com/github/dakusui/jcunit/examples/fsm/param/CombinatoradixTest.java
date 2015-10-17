package com.github.dakusui.jcunit.examples.fsm.param;

import com.github.dakusui.enumerator.Combinator;
import com.github.dakusui.enumerator.HomogeniousCombinator;
import com.github.dakusui.enumerator.Permutator;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * This is a test to make sure behaviours of a library (enumerator aka combinatoradix) that JCUnit uses
 */
public class CombinatoradixTest {
  private static final List<String> dataSet = Arrays.asList(
      /*
       1    2    3    4    5    6    7    8    9   10
       */
      "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
      "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
      "U", "V", "W", "X", "Y", "Z"
  );

  @Test
  public void test1000000thWord_Permutation() {
    assertEquals("[D, I, K, H, Q]", new Permutator<String>(dataSet, 5).get(1000000).toString());
  }

  @Test
  public void test1000000thWord_Combination() {
    assertEquals("[C, G, H, K, U, X, Y, Z]", new Combinator<String>(dataSet, 8).get(1000000).toString());
  }

  @Test
  public void test1000000thWord_RepeatedCombination() {
    assertEquals("[A, B, D, G, R, T, V, Z]", new HomogeniousCombinator<String>(dataSet, 8).get(1000000).toString());
  }
}

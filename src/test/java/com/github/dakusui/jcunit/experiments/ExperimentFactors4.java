package com.github.dakusui.jcunit.experiments;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.dakusui.jcunit.core.Generator;
import com.github.dakusui.jcunit.core.In;
import com.github.dakusui.jcunit.core.In.Domain;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.generators.BestPairwiseTestArrayGenerator;
import com.github.dakusui.jcunit.generators.CartesianTestArrayGenerator;
import com.github.dakusui.jcunit.generators.PairwiseTestArrayGenerator;
import com.github.dakusui.jcunit.generators.SimpleTestArrayGenerator;

@RunWith(JCUnit.class)
public class ExperimentFactors4 {
  @In(domain = Domain.Method)
  public int size;

  public static int[] size() {
    return new int[] { 10, 11, 13, 17, 20, 25 };
  }

  @In(domain = Domain.Method)
  public String color;

  public static String[] color() {
    return new String[] { "Black", "Red", "Blue", "Yellow" };
  }

  @In(domain = Domain.Method)
  public String family;

  public static String[] family() {
    return new String[] { "Serif", "Gothic", "Arial" };
  }

  @In(domain = Domain.Method)
  public String weight;

  public static String[] weight() {
    return new String[] { "normal", "bold" };
  }

  @Test
  public void test() {
    System.out.println(String
        .format("%s %s %s %d", family, weight, color, size));
  }

  @Generator(SimpleTestArrayGenerator.class)
  public static class WithSimple extends ExperimentFactors4 {
  }

  @Generator(PairwiseTestArrayGenerator.class)
  public static class WithSimplePairwise extends ExperimentFactors4 {
  }

  @Generator(BestPairwiseTestArrayGenerator.class)
  public static class WithBestPairwise extends ExperimentFactors4 {
  }

  @Generator(CartesianTestArrayGenerator.class)
  public static class WithCartesian extends ExperimentFactors4 {
  }

}

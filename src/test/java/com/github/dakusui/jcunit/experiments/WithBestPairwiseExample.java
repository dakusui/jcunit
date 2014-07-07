package com.github.dakusui.jcunit.experiments;

import com.github.dakusui.jcunit.compat.core.JCUnit;
import com.github.dakusui.jcunit.compat.generators.BestPairwiseTestArrayGenerator;
import com.github.dakusui.jcunit.core.Generator;
import com.github.dakusui.jcunit.core.In;
import com.github.dakusui.jcunit.core.In.Domain;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
@Generator(BestPairwiseTestArrayGenerator.class)
public class WithBestPairwiseExample {
  // @In(domain = Domain.Method)
  public int size;

  public static int[] size() {
    return new int[] { 10, 11, 13 /* , 17, 20, 25 */ };
  }

  @In(
      domain = Domain.Method)
  public String color;

  public static String[] color() {
    return new String[] { "Black", "Red", "Blue", "Yellow" };
  }

  @In(
      domain = Domain.Method)
  public String family;

  public static String[] family() {
    return new String[] { "Serif", "Gothic", "Arial", "XYZ" };
  }

  @In(
      domain = Domain.Method)
  public String weight;

  public static String[] weight() {
    return new String[] { "normal", "bold", "XYZ", "WXY" };
  }

  @Test
  public void test() {
    System.out.println(String
        .format("%s %s %s %d", family, weight, color, size));
  }
}

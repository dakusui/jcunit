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
public class Example2 {
  @In(
      domain = Domain.Method)
  public String a;

  public static String[] a() {
    return new String[] { "a1", "a2", "a3" };
  }

  @In(
      domain = Domain.Method)
  public String b;

  public static String[] b() {
    return new String[] { "b1", "b2", "b3" };
  }

  @In(
      domain = Domain.Method)
  public String c;

  public static String[] c() {
    return new String[] { "c1", "c2", "c3" };
  }

  @In(
      domain = Domain.Method)
  public String d;

  public static String[] d() {
    return new String[] { "d1", "d2", "d3" };
  }

  @Test
  public void test() {
  }
}

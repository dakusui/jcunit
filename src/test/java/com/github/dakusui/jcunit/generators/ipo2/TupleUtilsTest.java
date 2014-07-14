package com.github.dakusui.jcunit.generators.ipo2;

import com.github.dakusui.jcunit.core.Tuple;
import com.github.dakusui.jcunit.core.TupleUtils;
import org.junit.Test;

public class TupleUtilsTest {
  @Test
  public void subtuples_01() {
    Tuple t = new Tuple.Builder().put("A", "a1").build();
    System.out.println(TupleUtils.subtuplesOf(t));
  }

  @Test
  public void subtuples_02() {
    Tuple t = new Tuple.Builder().put("A", "a1").put("B", "b1").build();
    System.out.println(TupleUtils.subtuplesOf(t));
  }
}

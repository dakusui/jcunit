package com.github.dakusui.jcunit.tests.core;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.ututils.UTUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

public class FactorsTest {
  @Test
  public void buildFactors_1() {
    Factors.Builder b = new Factors.Builder();
    b.add(new Factor("Hello", Arrays.asList(new Object[] { "World", "!" })));
    Factors factors = b.build();
    assertEquals(1, factors.size());
  }

  @Test
  public void buildFactors_2() {
    Factors.Builder b = new Factors.Builder(
        Arrays.asList(
            new Factor("Hello", Arrays.asList(new Object[] { "World", "!" }))
        )
    );
    Factors factors = b.build();
    assertEquals(1, factors.size());
  }
}

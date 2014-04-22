package com.github.dakusui.jcunit.ut.ipo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.github.dakusui.jcunit.generators.ipo.IPOValuePair;

import org.junit.Test;

public class ValuePairTest {
  @Test
  public void shouldEquals_01() {
    IPOValuePair pair1 = new IPOValuePair(0, "0-A", 1, "1-A");
    IPOValuePair pair2 = new IPOValuePair(0, "0-A", 1, "1-A");

    assertTrue(pair1.equals(pair2));
  }

  @Test
  public void shouldEquals_02() {
    IPOValuePair pair1 = new IPOValuePair(0, "0-A", 1, "1-A");
    IPOValuePair pair2 = new IPOValuePair(1, "1-A", 0, "0-A");

    assertTrue(pair1.equals(pair2));
  }

  @Test
  public void shouldNotEqual_01() {
    IPOValuePair pair1 = new IPOValuePair(0, "0-A", 2, "1-A");
    IPOValuePair pair2 = new IPOValuePair(0, "0-A", 1, "1-A");

    assertFalse(pair1.equals(pair2));
  }

  @Test
  public void shouldNotEqual_02() {
    IPOValuePair pair1 = new IPOValuePair(0, "0-A", 1, "1-A");
    IPOValuePair pair2 = new IPOValuePair(0, "0-A", 1, "1-B");

    assertFalse(pair1.equals(pair2));
  }
}

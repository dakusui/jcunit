package com.github.dakusui.petronia.ut;

import com.github.dakusui.jcunit.core.JCUnitBase;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AlwaysTrueTest extends JCUnitBase {
  @Test
  public void alwaysTrue() throws JCUnitException, CUT {
    assertEquals(true, Basic.eval(this, any()));
  }
}

package com.github.dakusui.petronia.ut;

import com.github.dakusui.jcunit.compat.core.JCUnitBase;
import com.github.dakusui.jcunit.exceptions.JCUnitCheckedException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.exceptions.LisjCheckedException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AlwaysTrueTest extends JCUnitBase {
  @Test
  public void alwaysTrue() throws LisjCheckedException, CUT {
    assertEquals(true, Basic.eval(this, any()));
  }
}

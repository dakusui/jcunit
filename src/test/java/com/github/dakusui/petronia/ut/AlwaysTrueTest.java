package com.github.dakusui.petronia.ut;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.dakusui.jcunit.core.DefaultRuleSetBuilder;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;

public class AlwaysTrueTest extends DefaultRuleSetBuilder {
  @Test
  public void alwaysTrue() throws JCUnitException, CUT {
    assertEquals(true, Basic.eval(this, any()));
  }
}

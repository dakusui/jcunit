package com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base;

import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.pcond.provider.PreconditionViolationException;
import org.junit.Test;

import static com.github.dakusui.pcond.Preconditions.require;
import static com.github.dakusui.jcunit8.pipeline.stages.generators.ext.base.FactorSpaceNormalizer.isSupportedFactor;

public class FactorSpaceNormalizerTest {
  @Test(expected = PreconditionViolationException.class)
  public void test() {
    require(Factor.create("fName", new Object[] { 1, 2, 0 }), isSupportedFactor());
  }
}

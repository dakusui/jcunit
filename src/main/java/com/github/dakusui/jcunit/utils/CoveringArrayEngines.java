package com.github.dakusui.jcunit.utils;

import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.plugins.PluginUtils;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.caengines.IPO2CoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.runners.core.RunnerContext;

import java.util.Arrays;

/**
 * A collection of convenience methods to instantiate covering array engines.
 */
public enum CoveringArrayEngines {
  ;

  private static CoveringArrayEngine.Builder<String[]> createSimpleBuilder(
      Factors factors,
      ConstraintChecker constraintChecker,
      Class<? extends CoveringArrayEngine> engineClass,
      String[]... configArgsForEngine
  ) {
    return new CoveringArrayEngine.Builder<String[]>(
        new RunnerContext.Dummy(),
        factors,
        constraintChecker,
        engineClass
    ).setResolver(PluginUtils.StringArrayResolver.INSTANCE).setConfigArgsForEngine(Arrays.asList(configArgsForEngine));
  }

  public static CoveringArrayEngine.Builder<String[]> createSimpleBuilder(
      Factors factors,
      Class<? extends CoveringArrayEngine> engineClass,
      String[]... arguments) {
    return createSimpleBuilder(
        factors,
        ConstraintChecker.DEFAULT_CONSTRAINT_CHECKER,
        engineClass,
        arguments);
  }


  public static CoveringArrayEngine.Builder<String[]> createSimpleBuilder(Factors factors) {
    return createSimpleBuilder(factors, IPO2CoveringArrayEngine.class);
  }
}

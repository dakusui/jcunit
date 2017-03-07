package com.github.dakusui.jcunit.utils;

import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.plugins.PluginUtils;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.caengines.Ipo2CoveringArrayEngine;
import com.github.dakusui.jcunit.runners.core.RunnerContext;

import java.util.Arrays;

/**
 * A collection of convenience methods to instantiate covering array engines.
 */
public enum CoveringArrayEngines {
  ;

  public static CoveringArrayEngine.Builder<String[]> createSimpleBuilder(
      Factors factors,
      Class<? extends CoveringArrayEngine> engineClass,
      String[]... configArgsForEngine
  ) {
    return new CoveringArrayEngine.Builder<String[]>(
        RunnerContext.DUMMY,
        factors,
        engineClass
    ).setResolver(PluginUtils.StringArrayResolver.INSTANCE).setConfigArgsForEngine(Arrays.asList(configArgsForEngine));
  }

  public static CoveringArrayEngine.Builder<String[]> createSimpleBuilder(Factors factors) {
    return createSimpleBuilder(factors, Ipo2CoveringArrayEngine.class);
  }
}

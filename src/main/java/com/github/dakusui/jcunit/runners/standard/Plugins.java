package com.github.dakusui.jcunit.runners.standard;

import com.github.dakusui.jcunit.plugins.Plugin;
import com.github.dakusui.jcunit.plugins.levelsproviders.LevelsProvider;
import com.github.dakusui.jcunit.runners.core.RunnerContext;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.annotations.Value;

import java.util.Arrays;

public class Plugins {
  private Plugins() {
  }

  public static LevelsProvider levelsProviderOf(FactorField ann) {
    assert ann != null;
    //noinspection unchecked
    Plugin.Factory<LevelsProvider, Value> factory = new Plugin.Factory<LevelsProvider, Value>(
        (Class<LevelsProvider>) ann.levelsProvider(),
        new Value.Resolver(),
        new RunnerContext.Dummy()
    );
    //noinspection ConstantConditions
    return factory.create(Arrays.asList(ann.providerParams()));
  }
}

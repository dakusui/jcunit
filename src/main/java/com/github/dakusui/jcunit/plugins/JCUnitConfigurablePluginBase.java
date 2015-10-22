package com.github.dakusui.jcunit.plugins;

import com.github.dakusui.jcunit.standardrunner.annotations.Param;
import com.github.dakusui.jcunit.core.Checks;

public abstract class JCUnitConfigurablePluginBase implements JCUnitConfigurablePlugin {
  @Override
  public void init(Param[] params) {
    Checks.checknotnull(params);
    init(Param.Type.processParams(this.parameterTypes(), params));
  }

  /**
   * The implementations of this method must clarify the expectations for
   * {@code processedParams}.
   */
  protected abstract void init(Object[] processedParams);
}

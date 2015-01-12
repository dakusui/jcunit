package com.github.dakusui.jcunit.core;

public abstract class JCUnitConfigurablePluginBase implements JCUnitConfigurablePlugin {
  private Param[] params;

  @Override
  public void init(Param[] params) {
    Checks.checknotnull(params);
    init(ParamType.processParams(this.parameterTypes(), params));
  }

  /**
   * The implementations of this method must clarify the expectations for
   * {@code processedParams}.
   */
  protected abstract void init(Object[] processedParams);

  public Param[] getParams() {
    Checks.checknotnull(this.params != null, "The field 'params' isn't yet initialized.");
    return this.params;
  }
}

package com.github.dakusui.jcunit.plugins.levelsproviders;

import com.github.dakusui.jcunit.standardrunner.annotations.Param;
import com.github.dakusui.jcunit.plugins.JCUnitConfigurablePluginBase;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * {@inheritDoc}
 */
public abstract class LevelsProviderBase extends JCUnitConfigurablePluginBase implements LevelsProvider {
  protected final LinkedList<String> errors;

  protected LevelsProviderBase() {
    this.errors = new LinkedList<String>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Param.Type[] parameterTypes() {
    return new Param.Type[0];
  }

  final public List<String> getErrorsOnInitialization() {
    return Collections.unmodifiableList(this.errors);
  }
}

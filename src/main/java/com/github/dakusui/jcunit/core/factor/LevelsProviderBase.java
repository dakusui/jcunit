package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.JCUnitConfigurablePluginBase;
import com.github.dakusui.jcunit.core.ParamType;

import java.lang.reflect.Field;
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
  public ParamType[] parameterTypes() {
    return new ParamType[0];
  }

  final public List<String> getErrorsOnInitialization() {
    return Collections.unmodifiableList(this.errors);
  }
}

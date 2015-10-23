package com.github.dakusui.jcunit.plugins.levelsproviders;

import com.github.dakusui.jcunit.plugins.JCUnitPlugin;
import com.github.dakusui.jcunit.standardrunner.annotations.Arg;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * {@inheritDoc}
 */
public abstract class LevelsProviderBase extends JCUnitPlugin.Base implements LevelsProvider {
  protected final LinkedList<String> errors;

  protected LevelsProviderBase() {
    this.errors = new LinkedList<String>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Arg.Type[] parameterTypes() {
    return new Arg.Type[0];
  }

  final public List<String> getErrorsOnInitialization() {
    return Collections.unmodifiableList(this.errors);
  }
}

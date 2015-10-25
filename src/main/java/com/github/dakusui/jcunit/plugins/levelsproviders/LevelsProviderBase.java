package com.github.dakusui.jcunit.plugins.levelsproviders;

import com.github.dakusui.jcunit.plugins.Plugin;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * {@inheritDoc}
 */
public abstract class LevelsProviderBase extends Plugin.Base implements LevelsProvider {
  protected final LinkedList<String> errors;

  public LevelsProviderBase() {
    this.errors = new LinkedList<String>();
  }

  final public List<String> getErrorsOnInitialization() {
    return Collections.unmodifiableList(this.errors);
  }
}

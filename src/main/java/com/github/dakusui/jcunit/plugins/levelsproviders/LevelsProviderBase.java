package com.github.dakusui.jcunit.plugins.levelsproviders;

import com.github.dakusui.jcunit.plugins.Plugin;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * {@inheritDoc}
 */
public abstract class LevelsProviderBase<S> extends Plugin.Base implements LevelsProvider {
  protected final LinkedList<String> errors;

  public LevelsProviderBase(Param.Translator translator) {
    super(translator);
    this.errors = new LinkedList<String>();
  }

  public LevelsProviderBase() {
    this(Param.Translator.NULLTRANSLATOR);
  }

  final public List<String> getErrorsOnInitialization() {
    return Collections.unmodifiableList(this.errors);
  }
}

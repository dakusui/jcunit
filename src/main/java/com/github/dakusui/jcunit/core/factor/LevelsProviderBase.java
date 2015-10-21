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

  /**
   * {@inheritDoc}
   */
  @Override
  final public void init(Object[] parameters) {
    Field targetField = Checks.checknotnull(Checks.cast(Field.class, parameters[0]));
    this.errors.clear();
    init(targetField, parameters);
    Checks.checktest(
        this.errors.isEmpty(),
        "Errors are found in field '%s#%s'(type:%s) of '%s': %s",
        targetField.getDeclaringClass().getSimpleName(),
        targetField.getName(),
        targetField.getType().getSimpleName(),
        this.errors
    );
  }

  /**
   * This method is called by {@code init(Object[] parameters)} method.
   */
  protected abstract void init(
      Field targetField,
      Object[] parameters);

  final public List<String> getErrorsOnInitialization() {
    return Collections.unmodifiableList(this.errors);
  }
}

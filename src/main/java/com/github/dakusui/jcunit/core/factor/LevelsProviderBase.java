package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.JCUnitConfigurablePluginBase;
import com.github.dakusui.jcunit.core.ParamType;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * {@inheritDoc}
 */
public abstract class LevelsProviderBase<T> extends JCUnitConfigurablePluginBase implements LevelsProvider<T> {
  private         Field              targetField;
  private         FactorField        annotation;
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
    Checks.checknotnull(this.annotation);
    Checks.checknotnull(this.targetField);

    this.errors.clear();
    init(this.targetField, this.annotation, parameters);
    Checks.checktest(
        this.errors.isEmpty(),
        "Errors are found in field '%s' of '%s': %s",
        this.targetField.getName(),
        this.targetField.getDeclaringClass().getCanonicalName(),
        this.errors
    );
  }

  /**
   * This method is called by {@code init(Object[] parameters)} method.
   */
  protected abstract void init(Field targetField,
      FactorField annotation, Object[] parameters);

  /**
   * {@inheritDoc}
   */
  @Override
  final public void setTargetField(Field targetField) {
    Checks.checknotnull(targetField);
    this.targetField = targetField;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  final public void setAnnotation(FactorField ann) {
    this.annotation = ann;
  }

  final public List<String> getErrorsOnInitialization() {
    return Collections.unmodifiableList(this.errors);
  }
}

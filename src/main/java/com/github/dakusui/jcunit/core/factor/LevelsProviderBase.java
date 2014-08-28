package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.ParamType;
import com.github.dakusui.jcunit.core.Utils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * {@inheritDoc}
 */
public abstract class LevelsProviderBase<T> implements LevelsProvider<T> {
    private Field targetField;
    private FactorField annotation;
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
        Utils.checknotnull(this.annotation);
        Utils.checknotnull(this.targetField);

        this.errors.clear();
        init(this.targetField, this.annotation, parameters);
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

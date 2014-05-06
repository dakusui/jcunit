package com.github.dakusui.lisj;

import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;

import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.exceptions.SymbolNotFoundException;

public abstract class ContextImpl implements Context {
  private final Map<String, Object> formMap = new HashMap<String, Object>();

  @Override
  public Object lookup(Symbol symbol) throws SymbolNotFoundException {
    return lookup(symbol.name());
  }

  /*
   * Throws an exception if the given name isn't registered.
   */
  private Object lookup(String symbolName) throws SymbolNotFoundException {
    if (!formMap.containsKey(symbolName)) {
      String msg = String.format("The symbol '%s' wasn't found.", symbolName);
      throw new SymbolNotFoundException(msg, null);
    }
    return this.formMap.get(symbolName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object bind(Symbol symbol, Object value) {
    this.formMap.put(symbol.name(), value);
    return value;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void register(Form form, String... aliases) {
    // //
    // Preset forms are registered with an '*'.
    if (aliases.length == 0) {
      formMap.put(form.name(), form);
    } else {
      for (String s : aliases) {
        formMap.put(s, form);
      }
    }
  }

  @Override
  public MathContext bigDecimalMathContext() {
    return MathContext.DECIMAL128;
  }

  @Override
  public Context createChild() {
    return clone();
  }

  @Override
  protected Context clone() {
    try {
      return (Context) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public Lisj lisj() {
    return new Lisj(this);
  }

  @Override
  public void beginEvaluation(Form form, Object params_) {
  }

  @Override
  public void endEvaluation(Form form, FormResult ret) {
  }

  @Override
  public void failEvaluation(Form form, int index, JCUnitException e) {
  }

  @Override
  public void cutEvaluation(Form form, int index, CUT e) {
  }

  @Override
  public void eachEvaluation(BaseForm form, Object cur, FormResult ret) {
  }
}

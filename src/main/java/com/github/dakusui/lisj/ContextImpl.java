package com.github.dakusui.lisj;

import com.github.dakusui.jcunit.exceptions.SymbolNotFoundException;

import java.math.MathContext;
import java.util.*;

public abstract class ContextImpl implements Context {
  private final List<ContextObserver> observers = new LinkedList<ContextObserver>();
  private final Map<String, Object>   formMap   = new HashMap<String, Object>();

  @Override
  public Object lookup(Symbol symbol) throws SymbolNotFoundException {
    Object ret = null;
    try {
      ret = lookup(symbol.name());
    } finally {
      for (ContextObserver o : this.observers) {
        o.symbolEvaluation(symbol, ret);
      }
    }
    return ret;
  }

  /*
   * Throws an exception if the given name isn't registered.
   */
  private Object lookup(String symbolName) throws SymbolNotFoundException {
    if (!formMap.containsKey(symbolName)) {
      throw new SymbolNotFoundException(symbolName, null);
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
  public void addObserver(ContextObserver observer) {
    this.observers.add(observer);
  }

  @Override
  public void removeObserver(ContextObserver observer) {
    this.observers.remove(observer);
  }

  @Override
  public List<ContextObserver> observers() {
    return Collections.unmodifiableList(this.observers);
  }

  @Override
  public void clearObservers() {
    this.observers.clear();
  }

  @Override public boolean allowsUnboundSymbols() {
    return true;
  }
}

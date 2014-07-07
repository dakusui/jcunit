package com.github.dakusui.jcunit.core;

import java.math.MathContext;
import java.util.List;

import com.github.dakusui.jcunit.compat.auto.AutoRuleSet;
import com.github.dakusui.jcunit.compat.core.RuleSet;
import com.github.dakusui.jcunit.compat.core.RuleSetBuilder;
import com.github.dakusui.jcunit.exceptions.SymbolNotFoundException;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.ContextImpl;
import com.github.dakusui.lisj.ContextObserver;
import com.github.dakusui.lisj.Form;
import com.github.dakusui.lisj.Lisj;
import com.github.dakusui.lisj.Symbol;

public class JCUnitBase extends Lisj implements RuleSetBuilder, Context {
  Context context;

  public JCUnitBase() {
    // //
    // Doing a funky thing to create JCUnitBase instance, which extends Lisj and
    // makes it use itself as a context object, too.
    //
    // Call the super class's constructor with null, since Java prohibits
    // referencing 'this' during its execution.
    // Override init method to prevent the super class from accessing context.
    // And call super class's 'init' method implementation.
    super(null);
    this.context = new ContextImpl() {
    };
    super.init(this);
  }

  @Override
  protected void init(Context context) {
  }

  @Override
  public RuleSet ruleSet() {
    return ruleSet(this);
  }

  @Override
  public RuleSet ruleSet(Object target) {
    RuleSet ret = new RuleSet(this, target);
    return ret;
  }

  @Override
  public RuleSet autoRuleSet(Object obj, String... fields) {
    return new AutoRuleSet(this, obj, fields);
  }

  protected Object get(Object attrName) {
    return get(this, attrName);
  }

  protected Object set(Object attrName, Object value) {
    return set(this, attrName, value);
  }

  @Override
  public Context createChild() {
    return this.context.createChild();
  }

  @Override
  public MathContext bigDecimalMathContext() {
    return this.context.bigDecimalMathContext();
  }

  @Override
  public Object lookup(Symbol symbol) throws SymbolNotFoundException {
    return this.context.lookup(symbol);
  }

  @Override
  public Object bind(Symbol symbol, Object value) {
    return this.context.bind(symbol, value);
  }

  @Override
  public void register(Form form, String... aliases) {
    this.context.register(form, aliases);
  }

  @Override
  public Lisj lisj() {
    return this;
  }

  @Override
  public void addObserver(ContextObserver observer) {
    this.context.addObserver(observer);
  }

  @Override
  public void removeObserver(ContextObserver observer) {
    this.context.removeObserver(observer);
  }

  @Override
  public List<ContextObserver> observers() {
    return this.context.observers();
  }

  @Override
  public void clearObservers() {
    this.context.clearObservers();
  }

  @Override public boolean allowsUnboundSymbols() {
    return true;
  }
}

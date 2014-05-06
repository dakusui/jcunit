package com.github.dakusui.jcunit.core;

import java.math.MathContext;

import com.github.dakusui.jcunit.auto.AutoRuleSet;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.exceptions.SymbolNotFoundException;
import com.github.dakusui.lisj.BaseForm;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.ContextImpl;
import com.github.dakusui.lisj.Form;
import com.github.dakusui.lisj.FormResult;
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
  public void beginEvaluation(Form form, Object params_) {
    this.context.beginEvaluation(form, params_);
  }

  @Override
  public void endEvaluation(Form form, FormResult ret) {
    this.context.endEvaluation(form, ret);
  }

  @Override
  public void failEvaluation(Form form, int index, JCUnitException e) {
    this.context.failEvaluation(form, index, e);
  }

  @Override
  public void cutEvaluation(Form form, int index, CUT e) {
    this.context.cutEvaluation(form, index, e);
  }

  @Override
  public void eachEvaluation(BaseForm form, Object cur, FormResult ret) {
    this.context.eachEvaluation(form, cur, ret);
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
}

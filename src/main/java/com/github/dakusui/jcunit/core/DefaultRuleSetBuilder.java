package com.github.dakusui.jcunit.core;

import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.github.dakusui.jcunit.auto.AutoRuleSet;
import com.github.dakusui.jcunit.auto.OutFieldNames;
import com.github.dakusui.jcunit.exceptions.SymbolNotFoundException;
import com.github.dakusui.lisj.BaseForm;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.Form;
import com.github.dakusui.lisj.Symbol;
import com.github.dakusui.lisj.func.io.Print;
import com.github.dakusui.lisj.func.java.Get;
import com.github.dakusui.lisj.func.java.Invoke;
import com.github.dakusui.lisj.func.java.IsInstanceOf;
import com.github.dakusui.lisj.func.java.Set;
import com.github.dakusui.lisj.func.math.Add;
import com.github.dakusui.lisj.func.math.Div;
import com.github.dakusui.lisj.func.math.Max;
import com.github.dakusui.lisj.func.math.Min;
import com.github.dakusui.lisj.func.math.Mul;
import com.github.dakusui.lisj.func.math.NumCast;
import com.github.dakusui.lisj.func.math.Sub;
import com.github.dakusui.lisj.func.str.Concat;
import com.github.dakusui.lisj.func.str.Format;
import com.github.dakusui.lisj.pred.AlwaysTrue;
import com.github.dakusui.lisj.pred.And;
import com.github.dakusui.lisj.pred.Contains;
import com.github.dakusui.lisj.pred.Eq;
import com.github.dakusui.lisj.pred.Gt;
import com.github.dakusui.lisj.pred.IsOneOf;
import com.github.dakusui.lisj.pred.Matches;
import com.github.dakusui.lisj.pred.Not;
import com.github.dakusui.lisj.pred.Or;
import com.github.dakusui.lisj.pred.Same;
import com.github.dakusui.lisj.special.Assign;
import com.github.dakusui.lisj.special.Cond;
import com.github.dakusui.lisj.special.Eval;
import com.github.dakusui.lisj.special.Lambda;
import com.github.dakusui.lisj.special.Loop;
import com.github.dakusui.lisj.special.Progn;
import com.github.dakusui.lisj.special.Quote;
import com.github.dakusui.lisj.special.When;

public class DefaultRuleSetBuilder implements RuleSetBuilder, Context {
  private Map<String, Object> formMap = new HashMap<String, Object>();

  public DefaultRuleSetBuilder() {
    registerPresetForm(new Add());
    registerPresetForm(new And());
    registerPresetForm(new Assign());
    registerPresetForm(new AlwaysTrue(), "any");
    registerPresetForm(NumCast.bigDecimal());
    registerPresetForm(NumCast.bigInteger());
    registerPresetForm(new Cond());
    registerPresetForm(new Contains());
    registerPresetForm(NumCast.byteValue());
    registerPresetForm(new Div());
    registerPresetForm(NumCast.doubleValue());
    registerPresetForm(new Same());
    registerPresetForm(new Eval());
    registerPresetForm(new Eq());
    registerPresetForm(NumCast.floatValue());
    registerPresetForm(new Format());
    registerPresetForm(new Or() {
      private static final long serialVersionUID = 1L;

      @Override
      public Object bind(final Object... params) {
        // Since bind method destroys params, need to pass a cloned array to the
        // bind
        // calls but the last one.
        return super.bind(new Gt().bind(params.clone()),
            new Same().bind(params));
      }

      @Override
      public String name() {
        return "ge";
      }
    });
    registerPresetForm(new Get());
    registerPresetForm(new Gt());
    registerPresetForm(NumCast.intValue());
    registerPresetForm(new Invoke());
    registerPresetForm(new IsOneOf(), "is", "isoneof");
    registerPresetForm(new Lambda());
    registerPresetForm(new Not() {
      private static final long serialVersionUID = 1L;

      @Override
      public Object bind(final Object... params) {
        // In order to make bind method understand that 'one cons cell' is
        // being given, we need to pass it packing into an array of
        // java.lang.Object.
        return super.bind(new Gt().bind(params));
      }

      @Override
      public String name() {
        return "le";
      }
    });
    registerPresetForm(NumCast.longValue());
    registerPresetForm(new Loop());
    registerPresetForm(new Not() {
      private static final long serialVersionUID = 1L;

      @Override
      public Object bind(final Object... params) {
        // In order to make bind method understand that 'one cons cell' is
        // being given, we need to pass it packing into an array of
        // java.lang.Object.
        return super.bind(new Or() {
          private static final long serialVersionUID = 1L;

          @Override
          public Object bind(Object... params_) {
            return super.bind(new Gt().bind(params_.clone()),
                new Same().bind(params_));
          }

          @Override
          public String name() {
            return "or";
          }
        }.bind(params));
      }

      @Override
      public String name() {
        return "lt";
      }
    });
    registerPresetForm(new Matches());
    registerPresetForm(new Max());
    registerPresetForm(new Min());
    registerPresetForm(new Mul());
    registerPresetForm(new Not() {
      private static final long serialVersionUID = 1L;

      @Override
      public Object bind(final Object... params) {
        // In order to make bind method understand that 'one cons cell' is
        // being given, we need to pass it packing into an array of
        // java.lang.Object.
        return super.bind(new Same().bind(params));
      }

      @Override
      public String name() {
        return "ne";
      }
    });
    registerPresetForm(new Not());
    registerPresetForm(new Or());
    registerPresetForm(new Print());
    registerPresetForm(new Progn());
    registerPresetForm(new Quote(), "quote", "q");
    registerPresetForm(new Set());
    registerPresetForm(new Sub());
    registerPresetForm(NumCast.shortValue());
    registerPresetForm(new When());
    registerPresetForm(new IsInstanceOf());
    registerPresetForm(new Concat());
    registerPresetForm(new OutFieldNames());
  }

  private void registerPresetForm(Form form, String... aliases) {
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
  public Object add(Object... params) {
    return form("add").bind(params);
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
  public Object and(Object... args) {
    return form("and").bind(args);
  }

  @Override
  public Object any() {
    return form("any").bind();
  }

  @Override
  public Object bigDecimal(Object num) {
    return form("bigDecimal").bind(num);
  }

  @Override
  public Object bigInteger(Object num) {
    return form("bigInteger").bind(num);
  }

  @Override
  public Object byteValue(Object num) {
    return form("byteValue").bind(num);
  }

  @Override
  public Object contains(Object obj, String str) {
    return form("contains").bind(obj, str);
  }

  @Override
  public Object concat(Object separator, Object... args) {
    return form("concat").bind(ArrayUtils.add(args, 0, separator));
  }

  @Override
  public Object div(Object... params) {
    return form("div").bind(params);
  }

  @Override
  public Object doubleValue(Object num) {
    return form("doubleValue").bind(num);
  }

  @Override
  public Object isinstanceof(Object obj, Object clazz) {
    return form("isinstanceof").bind(obj, clazz);
  }

  @Override
  public Object same(Object obj, Object another) {
    return form("same").bind(obj, another);
  }

  @Override
  public Object ne(Object obj, Object another) {
    return form("ne").bind(obj, another);
  }

  @Override
  public Object floatValue(Object num) {
    return form("floatValue").bind(num);
  }

  @Override
  public Object ge(Object obj, Object another) {
    return form("ge").bind(obj, another);
  }

  protected Object get(Object attrName) {
    return get(this, attrName);
  }

  @Override
  public Object get(Object obj, Object attrName) {
    return form("get").bind(obj, attrName);
  }

  @Override
  public Object gt(Object obj, Object another) {
    Object ret = (Object[]) form("gt").bind(obj, another);
    return ret;
  }

  @Override
  public Object intValue(Object num) {
    return form("intValue").bind(num);
  }

  public Object invoke(Object obj, String methodId) {
    return form("invoke").bind(obj, methodId);
  }

  public Object invoke(String methodId) {
    return invoke(this, methodId);
  }

  @Override
  public Object eq(Object obj, Object arg) {
    Object[] ret = (Object[]) form("eq").bind(new Object[] { obj, arg });
    return ret;
  }

  @Override
  public Object is(Object obj, Object arg) {
    Object[] ret = (Object[]) form("is").bind(new Object[] { obj, arg });
    return ret;
  }

  @Override
  public Object isoneof(Object obj, Object... args) {
    Object[] ret = (Object[]) form("isoneof")
        .bind(ArrayUtils.add(args, 0, obj));
    return ret;
  }

  @Override
  public Object le(Object obj, Object another) {
    return form("le").bind(obj, another);
  }

  @Override
  public Object longValue(Object num) {
    return form("longValue").bind(num);
  }

  @Override
  public Object lt(Object obj, Object another) {
    return form("lt").bind(obj, another);
  }

  @Override
  public Object matches(Object obj, String regex) {
    return form("matches").bind(obj, regex);
  }

  @Override
  public Object max(Object... params) {
    return form("max").bind(params);
  }

  @Override
  public Object min(Object... params) {
    return form("min").bind(params);
  }

  @Override
  public Object mul(Object... params) {
    return form("mul").bind(params);
  }

  @Override
  public Object not(Object target) {
    return form("not").bind(target);
  }

  @Override
  public Object or(Object... args) {
    return ((BaseForm) form("or")).bind(args);
  }

  protected Object set(Object attrName, Object value) {
    return set(this, attrName, value);
  }

  @Override
  public Object set(Object obj, Object attrName, Object value) {
    return form("set").bind(obj, attrName, value);
  }

  @Override
  public Object shortValue(Object num) {
    return form("shortValue").bind(num);
  }

  @Override
  public Object sub(Object... params) {
    return form("sub").bind(params);
  }

  public Object outFieldNames() throws SymbolNotFoundException {
    return outFieldNames(this);
  }

  @Override
  public Object outFieldNames(Object obj) {
    return form("outfieldnames").bind(obj);
  }

  @Override
  public Object lambda(final Symbol param, final Object... funcBody) {
    return lambda(new Symbol[] { param }, funcBody);
  }

  @Override
  public Object lambda(final Symbol[] params, final Object... funcBody) {
    return form("lambda").bind(ArrayUtils.add(funcBody, 0, params));
  }

  @Override
  public Object lookup(Symbol symbol) throws SymbolNotFoundException {
    return lookup(symbol.name());
  }

  private Object lookup(String symbolName) throws SymbolNotFoundException {
    if (!formMap.containsKey(symbolName)) {
      String msg = String.format("The symbol '%s' wasn't found.", symbolName);
      throw new SymbolNotFoundException(msg, null);
    }
    return this.formMap.get(symbolName);
  }

  @Override
  public Object bind(Symbol symbol, Object value) {
    this.formMap.put(symbol.name(), value);
    return value;
  }

  @Override
  public Object eval(Object... args) {
    return form("eval").bind(args);
  }

  public Object q(Object... params) {
    return form("quote").bind(params);
  }

  public Object quote(Object... params) {
    return form("quote").bind(params);
  }

  @Override
  public Object cond(Object... whens) {
    return form("cond").bind((Object[]) whens);
  }

  @Override
  public Object when(Object pred, Object... statements) {
    return form("when").bind(ArrayUtils.add(statements, 0, pred));
  }

  @Override
  public Object assign(Symbol symbol, Object value) {
    return form("assign").bind(symbol, value);
  }

  @Override
  public Object print(Object s) {
    return form("print").bind(System.out, s);
  }

  @Override
  public Object loop(Object pred, Object... forms) {
    return form("loop").bind(ArrayUtils.add(forms, 0, pred));
  }

  @Override
  public Object progn(Object... forms) {
    return form("progn").bind((Object[]) forms);
  }

  @Override
  public Object format(Object format, Object... args) {
    return form("format").bind(ArrayUtils.add(args, 0, format));
  }

  /*
   * Returns a symbol whose name is specified by a parameter <code>name</code>.
   */
  @Override
  public Symbol $(String name) {
    Symbol ret = new Symbol(name);
    return ret;
  }

  @Override
  public Symbol[] $(String... names) {
    Symbol[] ret = new Symbol[names.length];
    int i = 0;
    for (String cur : names) {
      ret[i++] = new Symbol(cur);
    }
    return ret;
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
  public Context createChild() {
    return clone();
  }

  private Form form(String formName) {
    Form ret = null;
    try {
      // //
      // Preset forms are registered with an '*'.
      ret = (Form) lookup(formName);
    } catch (SymbolNotFoundException e) {
      assert false;
    }
    return ret;
  }

  @Override
  public MathContext bigDecimalMathContext() {
    return MathContext.DECIMAL128;
  }

  @Override
  public RuleSet autoRuleSet(Object obj, String... fields) {
    return new AutoRuleSet(this, obj, fields);
  }
}

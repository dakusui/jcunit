package com.github.dakusui.lisj;

import com.github.dakusui.jcunit.compat.auto.OutFieldNames;
import com.github.dakusui.jcunit.exceptions.SymbolNotFoundException;
import com.github.dakusui.lisj.func.io.Print;
import com.github.dakusui.lisj.func.java.Get;
import com.github.dakusui.lisj.func.java.Invoke;
import com.github.dakusui.lisj.func.java.IsInstanceOf;
import com.github.dakusui.lisj.func.java.Set;
import com.github.dakusui.lisj.func.math.*;
import com.github.dakusui.lisj.func.str.Concat;
import com.github.dakusui.lisj.func.str.Format;
import com.github.dakusui.lisj.pred.*;
import com.github.dakusui.lisj.special.*;
import org.apache.commons.lang3.ArrayUtils;

public class Lisj {
  private Context context;

  /**
   * Creates an object of this class initialized by the <code>context</code>
   * object.
   *
   * @param context A context object.
   */
  public Lisj(Context context) {
    init(context);
  }

  protected void init(Context context) {
    this.context = context;
    this.context.register(new Add());
    this.context.register(new And());
    this.context.register(new Assign());
    this.context.register(new AlwaysTrue(), "any");
    this.context.register(NumCast.bigDecimal());
    this.context.register(NumCast.bigInteger());
    this.context.register(new Cond());
    this.context.register(new Contains());
    this.context.register(NumCast.byteValue());
    this.context.register(new Div());
    this.context.register(NumCast.doubleValue());
    this.context.register(new Same());
    this.context.register(new Eval());
    this.context.register(new Eq());
    this.context.register(NumCast.floatValue());
    this.context.register(new Format());
    this.context.register(new Or() {
      private static final long serialVersionUID = 1L;

      @Override
      public Object bind(final Object... params) {
        // Since bind method destroys params, need to pass a cloned array to the
        // bind
        // calls but the last one.
        return super
            .bind(new Gt().bind(params.clone()), new Same().bind(params));
      }

      @Override
      public String name() {
        return "ge";
      }
    });
    this.context.register(new Get());
    this.context.register(new Gt());
    this.context.register(NumCast.intValue());
    this.context.register(new Invoke());
    this.context.register(new IsOneOf(), "is", "isoneof");
    this.context.register(new Lambda());
    this.context.register(new Not() {
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
    this.context.register(NumCast.longValue());
    this.context.register(new Loop());
    this.context.register(new Not() {
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
            return super
                .bind(new Gt().bind(params_.clone()), new Same().bind(params_));
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
    this.context.register(new Matches());
    this.context.register(new Max());
    this.context.register(new Min());
    this.context.register(new Mul());
    this.context.register(new Not() {
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
    this.context.register(new Not());
    this.context.register(new Or());
    this.context.register(new Print());
    this.context.register(new Progn());
    this.context.register(new Quote(), "quote", "q");
    this.context.register(new Set());
    this.context.register(new Sub());
    this.context.register(NumCast.shortValue());
    this.context.register(new When());
    this.context.register(new IsInstanceOf());
    this.context.register(new Concat());
    this.context.register(new OutFieldNames());
  }

  public Object add(Object... params) {
    return form("add").bind(params);
  }

  public Object and(Object... args) {
    return form("and").bind(args);
  }

  public Object any() {
    return form("any").bind();
  }

  public Object bigDecimal(Object num) {
    return form("bigDecimal").bind(num);
  }

  public Object bigInteger(Object num) {
    return form("bigInteger").bind(num);
  }

  public Object byteValue(Object num) {
    return form("byteValue").bind(num);
  }

  public Object contains(Object obj, String str) {
    return form("contains").bind(obj, str);
  }

  public Object concat(Object separator, Object... args) {
    return form("concat").bind(ArrayUtils.add(args, 0, separator));
  }

  public Object div(Object... params) {
    return form("div").bind(params);
  }

  public Object doubleValue(Object num) {
    return form("doubleValue").bind(num);
  }

  public Object isinstanceof(Object obj, Object clazz) {
    return form("isinstanceof").bind(obj, clazz);
  }

  public Object same(Object obj, Object another) {
    return form("same").bind(obj, another);
  }

  public Object ne(Object obj, Object another) {
    return form("ne").bind(obj, another);
  }

  public Object floatValue(Object num) {
    return form("floatValue").bind(num);
  }

  public Object ge(Object obj, Object another) {
    return form("ge").bind(obj, another);
  }

  public Object get(Object obj, Object attrName) {
    return form("get").bind(obj, attrName);
  }

  public Object gt(Object obj, Object another) {
    Object ret = form("gt").bind(obj, another);
    return ret;
  }

  public Object intValue(Object num) {
    return form("intValue").bind(num);
  }

  public Object invoke(Object obj, String methodId) {
    return form("invoke").bind(obj, methodId);
  }

  public Object invoke(String methodId) {
    return invoke(this, methodId);
  }

  public Object eq(Object obj, Object arg) {
    Object[] ret = (Object[]) form("eq").bind(new Object[] { obj, arg });
    return ret;
  }

  public Object is(Object obj, Object arg) {
    Object[] ret = (Object[]) form("is").bind(new Object[] { obj, arg });
    return ret;
  }

  public Object isoneof(Object obj, Object... args) {
    Object[] ret = (Object[]) form("isoneof")
        .bind(ArrayUtils.add(args, 0, obj));
    return ret;
  }

  public Object le(Object obj, Object another) {
    return form("le").bind(obj, another);
  }

  public Object longValue(Object num) {
    return form("longValue").bind(num);
  }

  public Object lt(Object obj, Object another) {
    return form("lt").bind(obj, another);
  }

  public Object matches(Object obj, String regex) {
    return form("matches").bind(obj, regex);
  }

  public Object max(Object... params) {
    return form("max").bind(params);
  }

  public Object min(Object... params) {
    return form("min").bind(params);
  }

  public Object mul(Object... params) {
    return form("mul").bind(params);
  }

  public Object not(Object target) {
    return form("not").bind(target);
  }

  public Object or(Object... args) {
    return ((BaseForm) form("or")).bind(args);
  }

  public Object set(Object obj, Object attrName, Object value) {
    return form("set").bind(obj, attrName, value);
  }

  public Object shortValue(Object num) {
    return form("shortValue").bind(num);
  }

  public Object sub(Object... params) {
    return form("sub").bind(params);
  }

  public Object outFieldNames() throws SymbolNotFoundException {
    return outFieldNames(this);
  }

  public Object outFieldNames(Object obj) {
    return form("outfieldnames").bind(obj);
  }

  public Object lambda(final Symbol param, final Object... funcBody) {
    return lambda(new Symbol[] { param }, funcBody);
  }

  public Object lambda(final Symbol[] params, final Object... funcBody) {
    return form("lambda").bind(ArrayUtils.add(funcBody, 0, params));
  }

  public Object eval(Object... args) {
    return form("eval").bind(args);
  }

  public Object q(Object... params) {
    return form("quote").bind(params);
  }

  public Object quote(Object... params) {
    return form("quote").bind(params);
  }

  public Object cond(Object... whens) {
    return form("cond").bind(whens);
  }

  public Object when(Object pred, Object... statements) {
    return form("when").bind(ArrayUtils.add(statements, 0, pred));
  }

  public Object assign(Symbol symbol, Object value) {
    return form("assign").bind(symbol, value);
  }

  public Object print(Object s) {
    return form("print").bind(System.out, s);
  }

  public Object loop(Object pred, Object... forms) {
    return form("loop").bind(ArrayUtils.add(forms, 0, pred));
  }

  public Object progn(Object... forms) {
    return form("progn").bind(forms);
  }

  public Object format(Object format, Object... args) {
    return form("format").bind(ArrayUtils.add(args, 0, format));
  }

  /*
   * Returns a symbol whose name is specified by a parameter <code>name</code>.
   */
  public Symbol $(String name) {
    Symbol ret = new Symbol(name);
    return ret;
  }

  public Symbol[] $(String... names) {
    Symbol[] ret = new Symbol[names.length];
    int i = 0;
    for (String cur : names) {
      ret[i++] = new Symbol(cur);
    }
    return ret;
  }

  private Form form(String formName) {
    Form ret = null;
    try {
      // //
      // Preset forms are registered with an '*'.
      ret = (Form) this.context.lookup(new Symbol(formName));
    } catch (SymbolNotFoundException e) {
      assert false;
    }
    return ret;
  }
}

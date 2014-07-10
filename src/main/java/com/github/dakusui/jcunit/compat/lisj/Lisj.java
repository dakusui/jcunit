package com.github.dakusui.jcunit.compat.lisj;

import com.github.dakusui.jcunit.compat.auto.OutFieldNames;
import com.github.dakusui.lisj.exceptions.SymbolNotFoundException;
import com.github.dakusui.lisj.Context;

public class Lisj extends com.github.dakusui.lisj.Lisj {
  /**
   * Creates an object of this class initialized by the <code>context</code>
   * object.
   *
   * @param context A context object.
   */
  public Lisj(Context context) {
    super(context);
  }

  protected void init(Context context) {
    super.init(context);
    this.context.register(new Get());
    this.context.register(new Set());
    this.context.register(new OutFieldNames());
  }
  public Object get(Object obj, Object attrName) {
    return form("get").bind(obj, attrName);
  }

  public Object set(Object obj, Object attrName, Object value) {
    return form("set").bind(obj, attrName, value);
  }

  public Object outFieldNames() throws SymbolNotFoundException {
    return outFieldNames(this);
  }
}

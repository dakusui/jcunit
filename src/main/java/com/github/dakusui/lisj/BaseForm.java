package com.github.dakusui.lisj;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.JCUnitException;

import java.util.LinkedList;
import java.util.TreeSet;

public abstract class BaseForm implements Form {
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = -4312318987582790305L;

  private final String name;

  public BaseForm() {
    this.name = this.getClass().getSimpleName().toLowerCase();
  }

  protected void cut(Object value) throws CUT {
    throw new CUT(this, value);
  }

  /**
   * A template method of a form evaluation.
   */
  @Override
  final public Object evaluate(Context context, Object params_) throws JCUnitException, CUT {
    FormEvaluator evaluator = newEvaluator(context, params_);
    FormResult result = evaluator.result();
    try {
      while (evaluator.hasNext(result)) {
        result = evaluator.next(result);
      }
    } catch (CUT e) {
      if (!this.throwsCUT() && e.source() == this)
        result.value(e.value());
      else
        throw e;
    }
    return evaluator.evaluateLast(result).value();
  }

  abstract protected FormResult evaluateEach(Context context, Object currentParam, FormResult lastResult)
      throws JCUnitException, CUT;

  abstract protected FormResult evaluateLast(Context context, Object[] evaluatedParams, FormResult lastResult)
      throws JCUnitException, CUT;

  protected FormEvaluator newEvaluator(Context context, Object params) {
    /*
     * If it is sure that the form doesn't require a local context, you can give
     * a context to FormEvaluator's constructor instead of a new context created
     * by context.createChild method.
     */
    return new FormEvaluator(context.createChild(), this, checkParams(params), new FormResult(0, Basic.length(params),
        null, new LinkedList<Symbol>()));
  }

  protected Object checkParams(Object params) {
    return Utils.checknull(params);
  }

  @Override
  public String toString() {
    return this.name();
  }

  @Override
  public String name() {
    return this.name;
  }

  protected static FormResult evaluateEachSimply(Context context, Object currentParam, FormResult lastResult)
      throws JCUnitException, CUT {
    FormResult ret = lastResult;
    try {
      ret.value(Basic.eval(context, currentParam));
    } finally {
      ret.incrementPosition();
    }
    return ret;
  }

  /*
   * Creates an S-expression of the function call of this form from an array of
   * parameters. Hence the returned value is an S-expression.
   * 
   * Since this method may change the parameters contents, it shouldn't be
   * reused by callers.
   * 
   * <code>params</code> itself isn't an S-expression but just an array of
   * <code>java.lang.Object</code>. Though its elements can be S-expressions.
   * 
   * (non-Javadoc)
   * 
   * @see com.github.dakusui.lisj.Form#bind(java.lang.Object[])
   */
  @Override
  public Object bind(final Object... params) {
    // return ArrayUtils.add(params, 0, this);
    // //
    // Converting an array of S-expressions into an S-expressions that
    // represents
    // the original array.
    // Otherwise the semantics will be changed because the last element in the
    // array
    if (params.length > 1) {
      Object last = params[params.length - 1];
      if (!Basic.atom(last) && Basic.length(last) > 1)
        params[params.length - 1] = Basic.cons(params[params.length - 1], Basic.NIL);
    }
    Object ret;
    ret = Basic.cons(this, params);
    return ret;
  }

  /*
   * If a subclass of this class is designed to throw a 'CUT' to outside, it
   * should override this method and make it return true.
   */
  protected boolean throwsCUT() {
    return false;
  }

  protected String msgParameterLengthWrong(Object expectedLength, Object params) {
    return String.format("%s:The number of parameters must be %s (params=%s)", this, expectedLength,
        Basic.tostr(params));
  }

  protected String msgFirstParameterTypeMismatch(Object param) {
    return String.format("%s:The first parameter must be evaluated a boolean value, but '%s'(%s)", this, param,
        param != null ? param.getClass() : null);
  }

  protected String msgFirstParameterIsNull(Object params) {
    return String.format("%s:The first parameter cannot be null. (params=%s)", this, Basic.tostr(params));
  }

  protected String msgReturnedTypeMismatch(Class<?> expectedType, Object actualValue) {
    return String.format("%s:'%s' class value was expected, but '%s'(%s) was returned", this, expectedType.getName(),
        actualValue, actualValue == null ? null : actualValue.getClass().getName());
  }

  protected String msgTypeIncompatible(Comparable<?> lhs, Comparable<?> rhs) {
    return String.format("%s:Object '%s'(%s) and '%s'(%s) couldn't be compared each other.", this, lhs, lhs.getClass(),
        rhs, rhs.getClass());
  }

  protected String msgIllegalArgumentFound(Object value, Object[] params) {
    return String.format("%s:Illegal argument '%s'(%s) is found in given parameters '%s'", this, value,
        value == null ? null : value.getClass().getName(), Basic.tostr(params));
  }
}

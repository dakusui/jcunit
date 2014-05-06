package com.github.dakusui.lisj;

import com.github.dakusui.jcunit.exceptions.JCUnitException;

public final class FormEvaluator {
  private final Object params;
  private final BaseForm form;

  private Object[] evaluatedResult;
  private final Context context;
  private final FormResult result;
  private boolean initialized;

  public FormEvaluator(Context context, BaseForm func, Object params, FormResult formResult) {
    this.form = func;
    this.params = params;
    this.context = context;
    this.result = formResult;
    init();
  }

  public FormResult init() {
    this.context.beginEvaluation(this.form, this.params);
    this.evaluatedResult = new Object[Basic.length(this.params)];
    // //
    // do i really want this System.arraycopy?
    // the evaluated result is calculated by 'next' method one by one,
    // eventually.
    // right, if evaluateEach skips some element, the corresponding element in
    // evaluatedParams will remain null. but who cares?
    // it's the evaluated results in some meaning!
    //
    // ok, let's comment it out! (Aug/14)
    // System.arraycopy(this.params, 0, evaluatedResult, 0,
    // evaluatedResult.length);

    FormResult ret = new FormResult(0, evaluatedResult.length, null);
    this.initialized = true;
    return ret;
  }

  public boolean hasNext(FormResult lastResult) {
    if (!this.initialized)
      throw new IllegalStateException();
    // //
    // Since it's sure that this object is initialized, we can use
    // evaluatedResult's
    // length instead of the actual length of params, which needs some
    // calculation.
    return lastResult.nextPosition() < evaluatedResult.length;
  }

  public FormResult next(FormResult lastResult) throws CUT, JCUnitException {
    int nextPosition = lastResult.nextPosition();
    FormResult ret = null;
    try {
      Object cur = Basic.get(params, nextPosition);
      ret = form.evaluateEach(this.context, cur, lastResult);
      this.evaluatedResult[nextPosition] = ret.value();
      this.context.eachEvaluation(this.form, cur, ret);
      return ret;
    } catch (CUT e) {
      this.context.cutEvaluation(this.form, nextPosition, e);
      throw e;
    } catch (JCUnitException e) {
      this.context.failEvaluation(this.form, nextPosition, e);
      throw e;
    }
  }

  public FormResult evaluateLast(FormResult lastResult) throws JCUnitException, CUT {
    FormResult ret = null;
    try {
      ret = form.evaluateLast(this.context, this.evaluatedResult, lastResult);
      this.context.endEvaluation(this.form, ret);
      return ret;
    } catch (CUT e) {
      this.context.cutEvaluation(this.form, -1, e);
      throw e;
    } catch (JCUnitException e) {
      this.context.failEvaluation(this.form, -1, e);
      throw e;
    }
  }

  public FormResult result() {
    return this.result;
  }

}
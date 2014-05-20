package com.github.dakusui.lisj;

import com.github.dakusui.jcunit.exceptions.JCUnitException;

/**
 * A interface which represents an observer of a context to monitor Lisj's
 * execution behaviors.
 * 
 * @author hiroshi
 */
public interface ContextObserver {
  /**
   * A call back method which is executed when an evaluation procedure for a
   * given form begins.
   * 
   * @param form
   *          A form which is being evaluated.
   * @param params
   *          Parameters given to the form.
   */
  public void beginEvaluation(BaseForm form, Object params);

  /**
   * A call back method which is executed when an evaluation procedure for a
   * given form ends.
   * 
   * @param form
   *          A form which is being evaluated.
   * @param ret
   *          A form result returned by the evaluation process.
   */
  public void endEvaluation(BaseForm form, FormResult ret);

  /**
   * A call back method which is executed when an evaluation procedure for a
   * given form fails.
   * 
   * <code>index</code> gives the position of the <code>form</code>'s parameter
   * object being evaluated in <code>params</code> given to
   * <code>beginEvaluation</code> method, if this method is called during
   * parameter evaluation phase of <code>FormEvaluator</code>. Otherwise it will
   * be negative number.
   * 
   * @param form
   *          A form which is being evaluated.
   * @param index
   *          An index of parameter object.
   * @param e
   *          An exception which made the evaluation fail.
   */
  public void failEvaluation(BaseForm form, int index, JCUnitException e);

  /**
   * A call back method which is executed when an evaluation procedure for a
   * given form is cut.
   * 
   * <code>index</code> gives the position of the <code>form</code>'s parameter
   * object being evaluated in <code>params</code> given to
   * <code>beginEvaluation</code> method, if this method is called during
   * parameter evaluation phase of <code>FormEvaluator</code>. Otherwise it will
   * be negative number.
   * 
   * @param form
   *          A form which is being evaluated.
   * @param index
   *          An index of parameter object.
   * @param e
   *          A <code>cut</code> object which cut the evaluation process.
   */
  public void cutEvaluation(BaseForm form, int index, CUT e);

  /**
   * A call back method which is executed when an evaluation procedure for a
   * given form's each parameter is evaluated.
   * 
   * @param form
   *          A form which is being evaluated.
   * @param cur
   *          A parameter of <code>form</code> which is being processed.
   * @param ret
   *          A form result returned by the evaluation process.
   */
  public void eachEvaluation(BaseForm form, Object cur, FormResult ret);

  /**
   * A call back method which is executed when an evaluation procedure for a
   * given symbol is evaluated.
   * 
   * @param symbol
   *          A symbol which is evaluated.
   * @param value
   *          The symbol's value.
   */
  public void symbolEvaluation(Symbol symbol, Object value);
}

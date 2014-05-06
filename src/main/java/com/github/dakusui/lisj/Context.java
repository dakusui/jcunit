package com.github.dakusui.lisj;

import java.math.MathContext;

import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.exceptions.SymbolNotFoundException;

/**
 * This interface represents a naming context, which consists of a set of
 * name-to-object bindings, and an observer interface to monitor Lisj's
 * execution behaviors. It contains methods for examining, updating, and
 * monitoring these bindings and behaviors.
 */
public interface Context extends Cloneable {
  /**
   * Creates a new context which is a child of this context, which means that
   * the returned context will take over all the symbols and can also have new
   * symbols have.
   * 
   * But the changes made on the new context will be reflected to the original
   * context.
   * 
   * @return A new context.
   */
  public Context createChild();

  /**
   * A math context object, which should be used for computation of big
   * decimals.
   * 
   * @return A math context object.
   */
  public MathContext bigDecimalMathContext();

  /**
   * Returns a <code>Lisj</code> object, which creates Lisj's S-expression
   * objects using this objects as its context.
   * 
   * @see Lisj
   */
  public Lisj lisj();

  /**
   * A call back method which is executed when an evaluation procedure for a
   * given form begins.
   * 
   * @param form
   *          A form which is being evaluated.
   * @param params
   *          Parameters given to the form.
   */
  public void beginEvaluation(Form form, Object params);

  /**
   * A call back method which is executed when an evaluation procedure for a
   * given form ends.
   * 
   * @param form
   *          A form which is being evaluated.
   * @param ret
   *          A form result returned by the evaluation process.
   */
  public void endEvaluation(Form form, FormResult ret);

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
  public void failEvaluation(Form form, int index, JCUnitException e);

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
  public void cutEvaluation(Form form, int index, CUT e);

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
   * Returns an object bound with given symbol. If no value is bound with the
   * symbol, an exception will be thrown.
   * 
   * @param symbol
   *          A symbol
   * @return An object bound with given symbol.
   * @throws SymbolNotFoundException
   *           The symbol wasn't bound.
   */
  public Object lookup(Symbol symbol) throws SymbolNotFoundException;

  /**
   * Binds a <code>symbol</code> with a <code>value</code>. And unlike Java's
   * <code>HashMap.put</code> method, this method returns the newly registered
   * <code>value</code>.
   * 
   * @param symbol
   *          A symbol to be registered to this context.
   * @param value
   *          A value to be bound.
   * @return A bound value.
   */
  public Object bind(Symbol symbol, Object value);

  /**
   * Registers the given form to this object as a preset form. If no aliases are
   * given, <code>Form.name</code> method will be invoked on <code>form</code>
   * and the returned value will be used as an alias.
   * 
   * @param form
   *          A form to be registered.
   * @param aliases
   *          Aliases for the form.
   */
  public void register(Form form, String... aliases);
}

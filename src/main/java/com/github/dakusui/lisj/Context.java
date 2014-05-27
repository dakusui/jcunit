package com.github.dakusui.lisj;

import java.math.MathContext;
import java.util.List;

import com.github.dakusui.jcunit.exceptions.SymbolNotFoundException;

/**
 * This interface represents a naming context, which consists of a set of
 * name-to-object bindings. It contains methods for examining and updating these
 * bindings.
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

  /**
   * Adds an observer to this context.
   * 
   * @param observer
   *          An observer to be added.
   */
  public void addObserver(ContextObserver observer);

  /**
   * Removes an observer from this context.
   * 
   * @param observer
   *          An observer to be removed.
   */
  public void removeObserver(ContextObserver observer);

  /**
   * Returns an unmodifiable version of list of observers that this context has.
   * 
   * @return A list of observers.
   */
  public List<ContextObserver> observers();

  /**
   * Clears all the registered observers.
   */
  public void clearObservers();

  /**
   * If this method returns {@code true}, logical multi-nominal predicates,
   * ({@code And} and {@code Or}, will not throw a symbol not found exception
   * immediately in case it finds an unbound symbol.
   *
   * Instead, it will try the next parameter to determine its value and if it cannot
   * determine the value, A symbol not found exception will be thrown.
   *
   * @return true - allows unbound symbols if it's conceded by short cur / false - always report it immediately.
   */
  boolean allowsUnboundSymbols();
}

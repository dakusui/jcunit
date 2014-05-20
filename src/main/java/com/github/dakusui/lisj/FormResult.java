package com.github.dakusui.lisj;

import java.util.*;

public class FormResult {
  private int    nextPosition;
  private Object value;
  private int    numPositions;
  private List<Symbol> involvedSymbols;
  private int markedPosition;


  public FormResult(int nextPosition, int numPositions, Object value, List<Symbol> involvedSymbols) {
    this.nextPosition = nextPosition;
    this.numPositions = numPositions;
    this.value = value;
    this.involvedSymbols = involvedSymbols;
  }

  public int nextPosition() {
    return nextPosition;
  }

  public void nextPosition(int nextPosition) {
    this.nextPosition = nextPosition;
  }

  public Object value() {
    return value;
  }

  public void value(Object value) {
    this.value = value;
  }

  public void incrementPosition() {
    this.nextPosition++;
  }

  public int numPositions() {
    return this.numPositions;
  }

  /**
   * Returns a list of symbols involved in a form evaluation.
   *
   * @return A list of symbols involved in a form evaluation.
   */
  public SortedSet<Symbol> involvedSymbols() {
    SortedSet<Symbol> ret = new TreeSet<Symbol>();
    ret.addAll(this.involvedSymbols);
    return ret;
  }

  public void mark() {
    this.markedPosition = this.involvedSymbols.size();
  }

  public void reset() {
    this.involvedSymbols = this.involvedSymbols.subList(0, this.markedPosition);
  }
}
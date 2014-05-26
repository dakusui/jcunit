package com.github.dakusui.lisj;

import java.util.SortedSet;
import java.util.TreeSet;

public class FormResult {
  private int    nextPosition;
  private Object value;
  private int    numPositions;
  private int    markedPosition;


  public FormResult(int nextPosition, int numPositions, Object value) {
    this.nextPosition = nextPosition;
    this.numPositions = numPositions;
    this.value = value;
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
}
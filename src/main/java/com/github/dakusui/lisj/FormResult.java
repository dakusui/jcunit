package com.github.dakusui.lisj;

import com.github.dakusui.lisj.exceptions.LisjCheckedException;

import java.util.LinkedList;
import java.util.List;

public class FormResult {
  private int                          nextPosition;
  private Object                       value;
  private int                          numPositions;
  private List<LisjCheckedException> ignoredExceptions;

  public FormResult(int nextPosition, int numPositions, Object value) {
    this.nextPosition = nextPosition;
    this.numPositions = numPositions;
    this.value = value;
    this.ignoredExceptions = new LinkedList<LisjCheckedException>();
  }

  public int nextPosition() {
    return nextPosition;
  }

  public void nextPosition(int nextPosition) {
    this.nextPosition = nextPosition;
  }

  public void addIgnoredException(LisjCheckedException e) {
    this.ignoredExceptions.add(e);
  }

  public List<LisjCheckedException> ignoredExceptions() {
    return this.ignoredExceptions;
  }

  public void clearIgnoredExceptions() {
    this.ignoredExceptions.clear();
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
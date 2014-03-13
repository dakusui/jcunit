package com.github.dakusui.lisj.pred;

public class AlwaysTrue extends LogicalPredicate {
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 8848889664214466582L;

  @Override
  protected boolean initialValue() {
    return true;
  }
}

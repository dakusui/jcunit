package com.github.dakusui.lisj.pred;

public class Same extends Comp {
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = -7647864958681950805L;

  protected boolean evaluate(Object value, Object another) {
    @SuppressWarnings("rawtypes")
    Comparable lhs = (Comparable) value;
    @SuppressWarnings("rawtypes")
    Comparable rhs = (Comparable) another;
    if (lhs.getClass().isAssignableFrom(rhs.getClass())) {
      return compare(lhs, rhs) == 0;
    }
    if (rhs.getClass().isAssignableFrom(lhs.getClass())) {
      return compare(rhs, lhs) == 0;
    }
    throw new IllegalArgumentException(msgTypeIncompatible(lhs, rhs));
  }
}

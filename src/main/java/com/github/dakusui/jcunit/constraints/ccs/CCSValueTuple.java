package com.github.dakusui.jcunit.constraints.ccs;

import com.github.dakusui.jcunit.core.ValueTuple;

public class CCSValueTuple<T, U> extends ValueTuple<T, U> {

  /**
   * Serial Version UID
   */
  private static final long serialVersionUID = -5590996290948921892L;

  /**
   * Returns a new constraint object which is created by merging this object and
   * <code>another</code> object. If this object and it are not consistent,
   * <code>null</code> will be returned.
   * 
   * @param another
   *          A constraint object to be merged with this object.
   * @return A merged constraint object.
   */
  public CCSValueTuple<T, U> merge(CCSValueTuple<T, U> another) {
    if (another == null)
      throw new NullPointerException();
    CCSValueTuple<T, U> ret = new CCSValueTuple<T, U>();
    CCSValueTuple<T, U> left = this;
    CCSValueTuple<T, U> right = another;
    if (this.size() > another.size()) {
      left = another;
      right = this;
    }
    if (!check(left, right))
      return null;
    ret.putAll(this);
    ret.putAll(another);
    return ret;
  }

  private boolean check(CCSValueTuple<T, U> left, CCSValueTuple<T, U> right) {
    for (T key : left.keySet()) {
      if (!right.containsKey(key))
        continue;
      if (eq(left.get(key), right.get(key)))
        continue;
      return false;
    }
    return true;
  }

  private boolean eq(U a, U b) {
    if (a == null)
      return b == null;
    return a.equals(b);
  }
}

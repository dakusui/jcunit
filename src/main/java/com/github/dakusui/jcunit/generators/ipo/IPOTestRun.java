package com.github.dakusui.jcunit.generators.ipo;

/**
 * A class represents each test run in a <code>TestRunSet</code> object.
 * 
 * @author hiroshi
 */
public class IPOTestRun implements Cloneable {
  /**
   * A field which represents values of parameters.
   */
  Object[] v;

  /**
   * Creates an object of this class.
   * 
   * @param width
   *          the width of the object.
   */
  public IPOTestRun(int width) {
    this.v = new Object[width];
  }

  /**
   * Sets a value <code>v</code> to the parameter F<code>i</code>.
   * 
   * @param i
   *          ID of parameter.
   * @param v
   *          The value to set.
   */
  public void set(int i, Object v) {
    if (i == 0)
      throw new IllegalArgumentException();
    this.v[i - 1] = v;
  }

  /**
   * Returns a value of the parameter F<code>i</code>.
   * 
   * @param i
   *          ID of the parameter.
   * @return The value of parameter F<code>i</code> in this test run.
   */
  public Object get(int i) {
    if (i == 0)
      throw new IllegalArgumentException();
    return this.v[i - 1];
  }

  /**
   * Returns the number of parameters in this test run.
   * 
   * @return Number of parameters.
   */
  public int width() {
    return this.v.length;
  }

  /**
   * Returns a new <code>Run</code> object whose length is incremented from this
   * object. The values are copied to the new object.
   * 
   * @return A new <code>Run</code> object.
   */
  public IPOTestRun grow() {
    IPOTestRun ret = new IPOTestRun(this.width() + 1);
    System.arraycopy(this.v, 0, ret.v, 0, this.v.length);
    return ret;
  }

  /**
   * Returns a <code>String</code> representation of this object.
   */
  @Override
  public String toString() {
    String ret = "";
    String sep = "";
    for (Object obj : this.v) {
      ret += sep + obj;
      sep = ",";
    }
    return ret;
  }

  final public IPOTestRun clone() {
    try {
      return (IPOTestRun) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean covers(IPOValuePair pair) {
    if (pair == null)
      throw new NullPointerException();
    if (eq(this.get(pair.A), pair.r))
      return eq(this.get(pair.B), pair.s);
    return false;
  }

  private static boolean eq(Object v, Object w) {
    if (v == null)
      return w == null;
    return v.equals(w);
  }
}
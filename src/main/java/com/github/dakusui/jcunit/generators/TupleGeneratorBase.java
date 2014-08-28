package com.github.dakusui.jcunit.generators;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * An abstract base class that provides a basic implementation of {@code TupleGenerator}.
 * Users can create a new tuple generator by extending this class.
 */
public abstract class TupleGeneratorBase
    implements TupleGenerator {
  /**
   * Parameters provided by test writers through {@code params} in '@Generator'.
   * <p/>
   *
   * E.g., The values retrieved by processing '{@literal @}Param("FailedOnly"),...}' will be
   *       assigned to this field.
   * <pre>
   *   {@literal @}TupleGeneration(
   *     generator = {@literal @}Generator(
   *       value = RecordedTuplePlayer.class,
   *       params = {@literal @}Param("FailedOnly"), ...}
   *   ))
   * </pre>
   */
  protected Object[] params;
  private Factors factors = null;
  private long    size    = -1;
  private long    cur     = -1;
  private ConstraintManager constraintManager;
  private Class<?>          targetClass;

  /**
   * {@inheritDoc}
   */
  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasNext() {
    if (size < 0 || this.cur < 0) {
      throw new IllegalStateException();
    }
    return cur < size;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<Tuple> iterator() {
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Tuple next() {
    if (cur >= size) {
      throw new NoSuchElementException();
    }
    Tuple ret = get(cur);
    cur++;
    return ret;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  final public void setFactors(Factors factors) {
    this.factors = factors;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  final public Factors getFactors() {
    return this.factors;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  final public void setConstraintManager(ConstraintManager constraintManager) {
    this.constraintManager = constraintManager;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  final public ConstraintManager getConstraintManager() {
    return this.constraintManager;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  final public void init(Object[] processedParameters) {
    this.params = processedParameters;
    this.cur = 0;
    this.size = initializeTuples(processedParameters);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Tuple get(long cur) {
    Tuple.Builder b = new Tuple.Builder();
    for (String f : this.factors.getFactorNames()) {
      b.put(f, factors.get(f).levels.get(getIndex(f, cur)));
    }
    return b.build();
  }

  private int getIndex(String factorName, long testId) {
    Tuple testCase = getTuple((int) testId);
    Object l = testCase.get(factorName);
    List<Object> levels = getFactor(factorName).levels;
    int ret = levels.indexOf(l);
    if (ret < 0) {
      for (int i = 0; i < levels.size(); i++) {
        if (arrayEquals(l, levels.get(i))) {
          ret = i;
          break;
        }
      }
    }
    Checks.checkcond(ret >= 0,
        "'%s' was not found in factor '%s'. Failed to find '%s' in '%s'",
        l,
        factorName,
        TupleUtils.toString(new Tuple.Builder().put("obj", l).build()),
        levels
    );
    return ret;
  }

  private boolean arrayEquals(Object a, Object b) {
    if (a == null || b == null) return b == a;
    if (!a.getClass().isArray() || !b.getClass().isArray()) return a.equals(b);

    int lena = Array.getLength(a);
    if (lena != Array.getLength(b)) return false;
    for (int i = 0; i < lena; i++) {
      arrayEquals(Array.get(a, i), Array.get(b, i));
    }
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long nextId(long tupleId) {
    return (++tupleId < this.size()) ? tupleId : -1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long firstId() {
    return 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override public void setTargetClass(Class<?> klazz) {
    this.targetClass = klazz;
  }

  /**
   * {@inheritDoc}
   */
  @Override public Class<?> getTargetClass() {
    return this.targetClass;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long size() {
    if (this.size < 0) {
      throw new IllegalStateException();
    }
    return this.size;
  }

  public Factor getFactor(String factorName) {
    return this.factors.get(factorName);
  }

  /**
   * Returns a {@code Tuple} object corresponds to the given {@code tupleId}.
   */
  public abstract Tuple getTuple(int tupleId);

  /**
   * Implementation of this method must return a number of tuples (test cases)
   * generated by this object in total.
   * <p/>
   * @return A number of test cases
   */
  abstract protected long initializeTuples(
      Object[] params);
}



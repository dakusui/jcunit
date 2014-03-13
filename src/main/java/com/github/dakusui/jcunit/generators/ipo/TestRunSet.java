package com.github.dakusui.jcunit.generators.ipo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dakusui.enumerator.Combinator;
import com.github.dakusui.jcunit.generators.ipo.ValueTuple.Attr;
import com.github.dakusui.jcunit.generators.ipo.ValueTuple.ValueTriple;

/**
 * A class that represents the set of test runs.
 * 
 * @author hiroshi
 */
public class TestRunSet extends ArrayList<TestRun> {
  private static final Logger LOGGER = LoggerFactory
                                         .getLogger(TestRunSet.class);

  public static class Info {
    public int numHorizontalFallbacks;
    public int numVerticalFallbacks;
  }

  private static final long serialVersionUID = 1L;
  int                       width;
  private Info              info;
  public Set<ValueTriple>   uncoveredTriples;

  TestRunSet(int width, Set<ValueTriple> uncoveredTriples) {
    this.width = width;
    this.uncoveredTriples = uncoveredTriples;
  }

  /**
   * Returns number of parameters that are held by this object.
   * 
   * @return number of attributes.
   */
  public int width() {
    return this.width;
  }

  /**
   * Add the given <code>run</code> object to this object. If a value not to be
   * added is given, a <code>RuntimeException</code> will be thrown.
   * 
   * @param run
   *          the value to be added.
   */
  @Override
  public boolean add(TestRun run) {
    if (run == null)
      throw new NullPointerException();
    if (run.v == null)
      throw new NullPointerException();
    if (run.v.length != this.width)
      throw new IllegalArgumentException();
    Set<ValueTriple> triplesToBeCovered = triplesCoveredBy(run);

    // int i = 0;
    // for (ValueTriple cur : triplesToBeCovered) {
    // LOGGER.debug("    Some known triples:" + cur);
    // if (i++ >= 10)
    // break;
    // }

    this.uncoveredTriples.removeAll(triplesToBeCovered);
    LOGGER.debug("Remaining uncoveredTriples:{}", this.uncoveredTriples.size());
    return super.add(run);
  }

  private Set<ValueTriple> triplesCoveredBy(TestRun run) {
    List<Attr> tmpAttrs = new ArrayList<Attr>();
    // The index should be 1-origin.
    for (int i = 1; i <= run.width(); i++) {
      Attr attr = new Attr(i, run.get(i));
      if (attr.value != IPO.DC)
        tmpAttrs.add(attr);
    }
    Combinator<Attr> comb = new Combinator<Attr>(tmpAttrs, 3);
    Set<ValueTriple> triplesToBeCovered = new HashSet<ValueTriple>();
    for (List<Attr> cur : comb) {
      ValueTriple triple = new ValueTriple(cur);
      triplesToBeCovered.add(triple);
    }
    LOGGER.trace("Triples to be covered:{}", triplesToBeCovered);
    return triplesToBeCovered;
  }

  public int countTriplesNewlyCoveredBy(TestRun run) {
    int ret = 0;
    for (ValueTriple cur : triplesCoveredBy(run)) {
      if (this.uncoveredTriples.contains(cur))
        ret++;
    }
    LOGGER.debug("Triples newly covered by {}:{}", run, ret);
    return ret;
  }

  /**
   * Returns an array of parameter IDs by which you can call 'valueOf' method as
   * <code>F</code>.
   * 
   * @return An array of parameter ID's.
   */
  public int[] coveredParameters() {
    int[] ret = new int[this.width];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = i + 1;
    }
    return ret;
  }

  /**
   * Returns <code>i</code>th run in this set. Note that <code>i</code> is
   * 1-origin. not 0.
   * 
   * @param i
   *          an index to specify test run.
   * @return A <code>i</code>th test run
   */
  public TestRun getRun(int i) {
    if (i == 0)
      throw new IllegalArgumentException();
    return super.get(i - 1);
  }

  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer(256);
    for (TestRun testrun : this) {
      buf.append(testrun.toString());
      buf.append("\n");
    }
    return buf.toString();
  }

  public void setInfo(Info info) {
    this.info = info;
  }

  public Info getInfo() {
    return this.info;
  }
}
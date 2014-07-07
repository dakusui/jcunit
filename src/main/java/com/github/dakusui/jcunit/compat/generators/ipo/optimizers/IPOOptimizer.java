package com.github.dakusui.jcunit.compat.generators.ipo.optimizers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dakusui.jcunit.compat.generators.ipo.IPOTestRun;
import com.github.dakusui.jcunit.compat.generators.ipo.IPOTestRunSet;
import com.github.dakusui.jcunit.compat.generators.ipo.IPOTestSpace;
import com.github.dakusui.jcunit.compat.generators.ipo.IPOValuePair;

public abstract class IPOOptimizer {
  private static final Logger           LOGGER         = LoggerFactory
                                                           .getLogger(IPOOptimizer.class);
  protected IPOTestSpace                   space;
  protected Map<Object, Set<IPOValuePair>> hgCandidateMap = new HashMap<Object, Set<IPOValuePair>>();
  protected List<Object>                hgCandidates   = new ArrayList<Object>(
                                                           10);
  private int                           sizeKnownBestAP$;

  public IPOOptimizer(IPOTestSpace space) {
    this.space = space;
    LOGGER.debug("{} is instantiated.", this.getClass());
  }

  public final void clearHGCandidates() {
    this.hgCandidates.clear();
    this.hgCandidateMap.clear();
    this.sizeKnownBestAP$ = -1;
  }

  public final void addHGCandidate(Object v, Set<IPOValuePair> AP$$) {
    if (AP$$.size() >= this.sizeKnownBestAP$) {
      if (AP$$.size() > this.sizeKnownBestAP$) {
        this.clearHGCandidates();
        this.sizeKnownBestAP$ = AP$$.size();
      }
      hgCandidateMap.put(v, AP$$);
      hgCandidates.add(v);
    }
  }

  /**
   * Implementation of this method must update AP$ accordingly. That is, it must
   * set the pairs that are covered by setting its returned value in F specified
   * by <code>fieldId</code>to <code>testRun</code>.
   * 
   * Our IPO framework side algorithm guarantees that this method isn't called
   * if hgCandidates size is zero. So, it is always safe to access the 0th
   * element of the field from inside the implementation of this method.
   * 
   * @param AP$
   *          All pairs covered by choosing the value returned by this method.
   *          This value is always empty when this method is called.
   * @param currentTestRunSet
   *          A TestRunSet object that represents currently added test runs so
   *          far.
   * @param testRun
   *          A test run that gives the values of the other fields than
   *          <code>fieldId</code>
   * @param fieldId
   *          An id of field the returned value should be set to.
   * @return The value this method suggests to set to F specified by
   *         <code>fieldId</code>
   */
  public final Object getBestHGValue(Set<IPOValuePair> AP$,
      IPOTestRunSet currentTestRunSet, IPOTestRun testRun, int fieldId) {
    Object ret = bestValueFor(currentTestRunSet, testRun, fieldId);
    AP$.addAll(hgCandidateMap.get(ret));
    return ret;
  }

  public final int numHGCandidates() {
    return hgCandidates.size();
  }

  abstract protected Object bestValueFor(IPOTestRunSet currentTestRunSet,
      IPOTestRun testRun, int fieldId);

  abstract public Object optimizeInVG(IPOTestRunSet currentTestRunSet,
      IPOTestRun testRun, int i);

  public void init() {
  }

  abstract public IPOTestRunSet createTestRunSet(int width);
}

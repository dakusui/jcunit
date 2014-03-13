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

public class IPOOptimizer {
  private static final Logger LOGGER = LoggerFactory
                                         .getLogger(TestRunSet.class);
  protected TestSpace         space;
  private Set<ValueTriple>    uncoveredTriples;

  public IPOOptimizer(TestSpace space) {
    this.space = space;
  }

  public TestRunSet createTestRunSet(int width) {
    return new TestRunSet(width) {
      /**
       * Serial version UID.
       */
      private static final long serialVersionUID = 8227221457493688297L;

      @Override
      public boolean add(TestRun run) {
        Set<ValueTriple> triplesToBeCovered = triplesCoveredBy(run);

        IPOOptimizer.this.uncoveredTriples.removeAll(triplesToBeCovered);
        LOGGER.debug("Remaining uncoveredTriples:{}",
            IPOOptimizer.this.uncoveredTriples.size());

        return super.add(run);
      }
    };
  }

  public void init() {
    LOGGER.debug("Creating triples");
    this.uncoveredTriples = space.createAllTriples();
    LOGGER.debug("Triples created:{}", this.uncoveredTriples.size());
  }

  public Object optimizeInVG(TestRunSet currentTestRunSet, TestRun testRun,
      int i) {
    return optimize(currentTestRunSet, testRun, i);
  }

  public Object optimizeInHG(TestRunSet currentTestRunSet, TestRun testRun,
      int i, int[] candidates) {
    return optimize(currentTestRunSet, testRun, i);
  }

  private Object optimize(TestRunSet currentTestRunSet, TestRun testRun, int i) {
    Object v = IPO.DC;
    int coverings = -1;
    for (int j = 1; j <= this.space.domainOf(i).length; j++) {
      testRun.set(i, this.space.value(i, j));
      int tmpCoverings = this.countTriplesNewlyCoveredBy(testRun);
      if (tmpCoverings > coverings) {
        v = this.space.value(i, j);
        coverings = tmpCoverings;
      }
    }
    return v;
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

  private int countTriplesNewlyCoveredBy(TestRun run) {
    int ret = 0;
    for (ValueTriple cur : triplesCoveredBy(run)) {
      if (this.uncoveredTriples.contains(cur))
        ret++;
    }
    LOGGER.debug("Triples newly covered by {}:{}", run, ret);
    return ret;
  }

}

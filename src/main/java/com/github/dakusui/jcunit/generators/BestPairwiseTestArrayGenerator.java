package com.github.dakusui.jcunit.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dakusui.enumerator.Permutator;
import com.github.dakusui.jcunit.generators.ipo.IPO;
import com.github.dakusui.jcunit.generators.ipo.IPOTestRunSet;
import com.github.dakusui.jcunit.generators.ipo.TestSpace;
import com.github.dakusui.jcunit.generators.ipo.optimizers.GreedyIPOOptimizer;

public class BestPairwiseTestArrayGenerator<T, U> extends
    PairwiseTestArrayGenerator<T, U> {
  private static final Logger LOGGER = LoggerFactory
                                         .getLogger(BestPairwiseTestArrayGenerator.class);

  @Override
  protected IPOTestRunSet composeTestRunSet(Map<Integer, T> indexToKeyMap) {
    if (this.domains.size() == 0) {
      LOGGER
          .warn("The domains has no member, falling back to PairwiseTestArrayGenerator.");
      return super.composeTestRunSet(indexToKeyMap);
    }
    List<T> domainKeys = new ArrayList<T>(this.domains.size());
    domainKeys.addAll(this.domains.keySet());
    Permutator<T> permutator = new Permutator<T>(domainKeys, domainKeys.size());
    long numPermutations = permutator.size();
    IPOTestRunSet ret = null;
    // //
    // Id of domain key list, this should be the only information to be stored
    // in
    // local file store to regenerate best pairwise array. But it is a
    // future work. (Task).
    long domainKeyListId = -1;
    List<T> chosenList = null;
    for (long i = 0; i < numPermutations; i++) {
      List<T> candidateDomainKeyList = permutator.get(i);
      // //
      // Compose test space domains array to be passed to IPO.
      Object[][] testSpaceDomains = new Object[this.domains.size()][];
      int j = 0;
      for (T cur : candidateDomainKeyList) {
        testSpaceDomains[j++] = this.domains.get(cur);
      }
      // //
      // Let IPO generate covering array.
      TestSpace space = new TestSpace(testSpaceDomains);
      IPO ipo = new IPO(space, new GreedyIPOOptimizer(space));
      IPOTestRunSet cand = ipo.ipo();
      if (ret == null || ret.size() > cand.size()) {
        ret = cand;
        domainKeyListId = i;
        chosenList = candidateDomainKeyList;
      }
    }
    int i = 1;
    for (T cur : chosenList) {
      indexToKeyMap.put(i++, cur);
    }
    LOGGER.info("{}/{} was chosen.", domainKeyListId, numPermutations);
    return ret;
  }
}

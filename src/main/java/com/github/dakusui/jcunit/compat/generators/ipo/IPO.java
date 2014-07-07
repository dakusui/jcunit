package com.github.dakusui.jcunit.compat.generators.ipo;

import com.github.dakusui.jcunit.compat.generators.ipo.optimizers.IPOOptimizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This class provides an implementation of the algorithm described in the book
 * "Foundations of Software Testing: Fundamental Algorithms and Techniques".
 * <p/>
 * Chapter 4. Test Generation from Combinatorial Designs 11. Generating Covering
 * Arrays
 */
public class IPO {
  /**
   * A logger object.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(IPO.class);

  /**
   * The 'Don't care value', used in 'vg'(vertical growth) procedure.
   */
  public static final Object DC = new Object() {
    @Override
    public String toString() {
      return "D/C";
    }
  };

  /**
   * The test space.
   */
  IPOTestSpace space;

  /**
   * An optimizer object.
   */
  private IPOOptimizer optimizer;

  /**
   * Creates an object of this class.
   *
   * @param space the definition of the test space in which the test runs are
   *              executed.
   */
  public IPO(IPOTestSpace space, IPOOptimizer optimizer) {
    this.space = space;
    this.optimizer = optimizer;
  }

  /*
   * Input: (a) n >= 2: Number of input parameters and (b) number of values for
   * each parameter Output: CA: A set of parameter combinations such that all
   * pairs of values are covers at least once.
   */
  public IPOTestRunSet ipo() {
    // ///
    IPOTestRunSet.Info info = new IPOTestRunSet.Info();

    IPOTestRunSet CA;
    int n = this.space.numDomains();
    assert n >= 2;

    // //
    // Step 1. [Initialize] Generate all pairs for parameters F1 and F2.
    // Set T = {(r,s) | for all r <E> D(f1) and s <E> D(F2)}.
    // # <E> means 'exists in'.
    optimizer.init();

    IPOTestRunSet T = optimizer.createTestRunSet(2);
    int Fi = 1;
    for (Object vi : this.space.domainOf(1)) {
      int Fj = 2;
      for (Object vj : this.space.domainOf(2)) {
        IPOTestRun cur = new IPOTestRun(2);
        cur.set(Fi, vi);
        cur.set(Fj, vj);
        T.add(cur);
      }
    }

    // //
    // Step 2. [Possible termination] if n = 2, then set CA = T and terminate
    // the algorithm, else continue;
    if (n == 2) {
      CA = T;
      CA.setInfo(info);
      return CA;
    }

    // //
    // Step 3. [Add remaining parameters] Repeat the following steps for
    // Fk, k = 3, 4, ..., n

    // 3.1 [Horizontal growth] Replace each partial run (v1, v2,
    // vk-1) <E> T with (v1, v2, vk-1, vk), where vk is a suitably
    // selected value of parameter Fk. More details of this
    // this step are found in procedure HG described in the
    // function 'hg'.

    for (int Fk = 3; Fk <= n; Fk++) {
      T = hg(info, T, Fk);

      // 3.2 [Uncovered pairs] Compute the set U of all uncovered pairs
      // formed by pairing parameters Fi, 1 <= i <= k-1 and parameter
      // Fk.
      Set<IPOValuePair> U = computeSetU(T, Fk);
      // 3.3 [Vertical growth] If U is empty, then terminate this step,
      // else continue. For each uncovered pair u = (vj, vk) <E> U,
      // add a run (v1, v2, ..., vj,..., vk-1, vk) to T. Here, vj
      // and vk denote values of parameters Fj and Fk respectively.
      // More details of this step are found in procedure VG described
      // in the function 'vg'.
      if (U.isEmpty()) {
        continue;
      }
      T = vg(info, T, U);
    }

    // //
    // Return the composed test set.
    CA = T;
    CA.setInfo(info);
    return CA;
  }

  /*
   * Assuming T covers all possible pairs with in Fi and Fj, where 1 <= i <= j
   * <= k -1, compute all the possible value pair whose one parameter is Fk.
   */
  private Set<IPOValuePair> computeSetU(IPOTestRunSet T, int Fk) {
    Set<IPOValuePair> ret = new LinkedHashSet<IPOValuePair>();
    for (IPOTestRun t : T) {
      for (int Fi = 1; Fi <= T.width; Fi++) {
        Object r = t.get(Fi);
        for (Object s : this.space.domainOf(Fk)) {
          if (Fi == Fk) {
            continue;
          }
          IPOValuePair p = new IPOValuePair(Fi, r, Fk, s);
          if (lookUp(lookUp(T, Fi, r), Fk, s).size() == 0) {
            ret.add(p);
          }
        }
      }
    }
    return ret;
  }

  /*
   * Input: (a) A set of m >= 1 runs of the kind R = (v1, v2,..., vk-1), k > 2,
   * where vi, 1 <= i <= (k-1) is a value of parameter Fi. (b) Parameter F !=
   * Fi, 1 <= i <= (k - 1). Output: T': A set of runs (v1, v2,..., vk-1), k > 2
   * obtained by extending the runs in T that cover the maximum number of pairs
   * between parameter Fi,
   */
  private IPOTestRunSet hg(IPOTestRunSet.Info info, IPOTestRunSet R, int F) {
    int m = R.size();
    assert m >= 1;
    /*
     * 
     * D(F) = {l1, l2,..., lq}, q ≥ 1. t1, t2,..., tm denote the m ≥ 1 runs in
     * T. For a run t <E> T, where t = (v1, v2,..., vk−1), extend(t,v) = (v1,
     * v2,..., vk−1, v), where v is a value of parameter F. Given t = (v1,
     * v2,..., vk − 1) and v is a parameter value, pairs (extend (t,v)) = {(vi,
     * v2), 1 ≤ i ≤ (k − 1)}.
     */

    // Step 1.
    // Let AP = {(r, s)|, where r is a value of parameter Fi, 1 ≤ i ≤ (k − 1)
    // and s <E> D(F)}. Thus, AP is the set of all pairs formed by combining
    // parameters Fi, 1 ≤ i ≤ (k − 1), taken one at a time, with parameter F.
    //
    // What is AP?
    // int k_1 = R.width(); // k_1 denotes 'k-1'.
    Set<IPOValuePair> AP = new LinkedHashSet<IPOValuePair>();
    {
      int k_1 = R.width();
      int k = k_1 + 1;
      for (int i = 1; i <= k_1; i++) {
        for (Object r : space.domainOf(i)) {
          for (Object s : space.domainOf(k)) {
            IPOValuePair pair = new IPOValuePair(i, r, k, s);
            AP.add(pair);
          }
        }
      }
    }

    // Step 2.
    // Let T' = <0>. T' denotes the set of runs obtained by extending the runs
    // in T in the following steps:
    // # <0> is an empty set.
    /* ret is T' in the book */
    IPOTestRunSet ret = optimizer.createTestRunSet(R.width() + 1);

    // Step 3.
    // Let C = min(q, m). Here, C is the number of elements in the set T or
    // the set D(F), whichever is less.

    // m is the size of given test run set.
    // see. D(F) = {l1, l2,..., lq}, q ≥ 1. | q is the size of domain of F

    int C = Math.min(m, space.domainOf(F).length);

    // Step 4.
    // Repeat the next two substeps for j = 1 to C.
    for (int j = 1; j <= C; j++) {
      // 4.1 Let t'j= extend(tj, lj).
      IPOTestRun tj$ = R.getRun(j).grow();
      // tj should now be considered t'j since it's extended.
      //
      // Task: Mar/07/2014: Consider the case where space.value(F, j) is
      // inappropriate and make the test run 'invalid'. If that's
      // the case, some other value should be set depending on
      // the SUT's specification.
      tj$.set(F, space.value(F, j));
      // T′ = T′ <U> t'j. # <U> is 'union'.
      ret.add(tj$);

      // 4.2 AP = AP − pairs(t'j).
      for (int ii = 1; ii <= tj$.width(); ii++) {
        if (F == ii) {
          continue;
        }
        AP.remove(new IPOValuePair(ii, tj$.get(ii), F, space.value(F, j)));
      }
    }

    // Step 5.
    // If C = m then return T'.
    if (C == m) {
      printTestRunSet("HG-1", ret);
      return ret;
    }

    // Step 6.
    // We will now extend the remaining runs in T by values of parameter F
    // that cover the maximum pairs in AP.
    // Repeat the next four sub steps for j = C + 1 to m.
    for (int j = C + 1; j <= m; j++) {
      IPOTestRun tj = R.getRun(j);
      if (tj.width() < F) {
        // F is at most width of tj + 1
        tj = tj.grow();
      }
      // 6.1 Let AP′ = <0>
      Set<IPOValuePair> AP$ = new HashSet<IPOValuePair>();
      Object v$ = chooseBestValueForF(info, AP$, R, tj, F, AP);

      // 6.3 Let = extend(tj, v′). T′ = T′ ∪ .
      tj.set(F, v$);
      ret.add(tj);

      // 6.4 AP = AP − AP′.
      AP.removeAll(AP$);
    }
    printTestRunSet("HG-2", ret);
    return ret /* T' in the book */;
  }

  /**
   * Returns the best value for the specified field in the test run. And outputs
   * the set of pairs newly covered by using the value.
   *
   * @param info information object to which this method sets how many times
   *             optimizer works and so on.
   * @param AP$  Value pairs that newly covered by using returned object as a value
   *             for Fi.
   * @param R    Current test run set
   * @param tj   A test run that is going to be added.
   * @param F    Index to specify the field for which this method chooses a value.
   * @param AP   Value pairs yet to be covered.
   * @return The value to be used for Fi.
   */
  private Object chooseBestValueForF(IPOTestRunSet.Info info,
      Set<IPOValuePair> AP$,
      IPOTestRunSet R, IPOTestRun tj, int F, Set<IPOValuePair> AP) {
    Object v$; // space.value(F, 1);
    this.optimizer.clearHGCandidates();
    for (int k = 1; k <= space.domainOf(F).length; k++) {
      // and v' = l1.
      // 6.2 In this step, we find a value of F by which to extend run
      // tj. The value that adds the maximum pairwise coverage is
      // selected. Repeat the next two substeps for each v <E> D(F).
      Set<IPOValuePair> AP$$ = new HashSet<IPOValuePair>();
      // 6.2.1 AP'' = {(r,v)|, where r is a value in run tj}. Here
      // AP'' is the set of all new pairs formed when run tj is
      // extended by v.
      Object v = space.value(F, k);
      for (int Fi : R.coveredParameters()) {
        IPOValuePair p = new IPOValuePair(Fi, tj.get(Fi), F, v);
        // AP is 'All pairs yet to cover'.
        if (AP.contains(p)) {
          AP$$.add(p);
        }
      }
      // 6.2.2 If |AP''| > |AP′| then AP′ = AP'' and v′ = v.
      // NOTE: The book says |AP''| > |AP'| is the condition to update the
      // candidate,
      // But in my experience, the smallest example like 3^4, this approach
      // creates a bit bigger test case set. So I'm doing |AP''| >= |AP'|
      // here.
      this.optimizer.addHGCandidate(v, AP$$);
    }
    if (this.optimizer.numHGCandidates() > 0) {
      if (optimizer.numHGCandidates() > 1) {
        info.numHorizontalFallbacks++;
      }
      AP$.clear();
      v$ = this.optimizer.getBestHGValue(AP$, R, tj, F);
    } else {
      // Falls back to the first value in the domain.
      v$ = space.value(F, 1);
    }
    return v$;
  }

  private void printTestRunSet(String message, IPOTestRunSet testRunSet) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("*** " + message + " ***");
      LOGGER.debug(testRunSet.toString());
      LOGGER.debug("*** *** ***");
    }
  }

  /**
   * Returns a list of elements whose parameter <code>F</code>'s value is
   * <code>v</code>.
   *
   * @param runList A list from which matching runs are searched.
   * @param F       parameter ID.
   * @param v       the value of F.
   * @return The list of test runs which satisfy the given condition.
   */
  public static List<IPOTestRun> lookUp(List<IPOTestRun> runList, int F,
      Object v) {
    List<IPOTestRun> ret = new LinkedList<IPOTestRun>();
    for (IPOTestRun cur : runList) {
      // //
      // Only object-wise identical ones are considered 'matched'.
      if (cur.get(F) == v) {
        ret.add(cur);
      }
    }
    return ret;
  }

  // Step 3 of VG (vertical growth)
  // For each run t <E> T', replace any don't care entry by an arbitrarily
  // selected value of the corresponding parameter. Instead, one
  // could also select a value that maximizes the number of
  // higher-oder tuples such as triples.
  private IPOTestRunSet replaceDontCareValues(IPOTestRunSet.Info info,
      IPOTestRunSet testRunSet) {
    for (IPOTestRun run : testRunSet) {
      for (int i = 1; i <= run.width(); i++) {
        if (run.get(i) == DC) {
          // //
          // By convention the first value in the domain is 'the least
          // harmful value'.
          // # The 'first' is 1, not 0!
          //
          // Task: Mar/07/2014
          // Consider setting other values than the default value, which
          // means coming up an algorithm that gives an index other than '1'.
          // run.set(i, this.space.value(i, 1));

          Object v = optimizer.optimizeInVG(testRunSet, run, i);
          if (v == DC) {
            // If the best value can't be chosen, the first one will be picked
            // up. (fallback)
            run.set(i, this.space.value(i, 1));
          } else {
            run.set(i, v);
          }

          info.numVerticalFallbacks++;
        }
      }
    }
    return testRunSet;
  }

  /*
   * Input: (a) T:A set of m >= runs each of the kind (v1, v2,..., vk-1, vk), k
   * > 2, where vi, 1 <= i <= k is a value of parameter Fi. (b) The set MP of
   * all pairs (r, s), where r is a value of parameter Fi, 1 <= i <= (k-1), s
   * <E> D(Fk), and the pair (r, s) is not contained in any run in T Output: A
   * set of runs (v1, v2,..., vk-1, vk), k > 2 such that all pairs obtained by
   * combining values of parameter Fi, 1 <= i <= (k - 1) with parameter Fk are
   * covered.
   */
  private IPOTestRunSet vg(IPOTestRunSet.Info info, IPOTestRunSet T,
      Set<IPOValuePair> MP) {
    /*
     * D(F) = {l1, l2,..., Lq}, q >= 1 t1, t2,..., tm denote the m >= 1 runs in
     * T. (Ai.r, Bj.s) denotes a pair of values r and s that correspond to
     * parameters A and B, respectively. In run (v1, V2,..., Vi-1, *, vi+1,...,
     * vk), i < k, an "*" denotes a don't care value for parameter Fi. When
     * needed, we use dc instead of "*"
     */

    // Step 1. Let T' = <0>
    IPOTestRunSet T$ = optimizer.createTestRunSet(T.width);

    // Step 2. Add new tests to cover the uncovered pairs.
    // For each missing pair (Fi.r, Fk.s) <E> MP, 1 <= i < k,
    // repeat the next two substeps.
    for (IPOValuePair pair : MP) {
      // 2.1 If there exists a run
      // (v1, v2,..., vi-1, *, vi+1,..., vk-1, s) <E> T'
      // then replace it by the run
      // (v1, v2,..., vi-1, r, vi+1,..., vk-1, s)
      // and examine the next missing pair, else go to the next substep
      List<IPOTestRun> runsWhoseAis_rAndBisDC = lookUp(
          lookUp(T$, pair.A, pair.r),
          pair.B, DC);
      if (runsWhoseAis_rAndBisDC.size() > 0) {
        // Since parameters are processed from left to right, this
        // path will not be executed under usual conditions.
        runsWhoseAis_rAndBisDC.get(0).set(pair.B, pair.s);
      }
      List<IPOTestRun> runsWhoseBis_sAndAisDC = lookUp(lookUp(T$, pair.A, DC),
          pair.B, pair.s);
      if (runsWhoseBis_sAndAisDC.size() > 0) {
        runsWhoseBis_sAndAisDC.get(0).set(pair.A, pair.r);
      }

      // 2.2 Let
      // t = (dc1, dc2,..., dci-1, r, dci+1,..., dck-1, s),
      // 1 <= i < k
      // Add t to T'
      // ***
      // *** Additional 'if' statement to the original algorithm.
      // *** Before trying to add a new run based on uncovered pairs,
      // *** we should check if it is already covered by T'. Because
      // *** previous executions of this loop maybe covered it.
      // ***
      if (lookUp(lookUp(T$, pair.A, pair.r), pair.B, pair.s).size() == 0) {
        int k = T.width();
        IPOTestRun r = new IPOTestRun(k);
        for (int i = 1; i <= k; i++) {
          r.set(i, DC);
        }
        r.set(pair.A, pair.r);
        r.set(pair.B, pair.s);
        T$.add(r);
      }
    }
    // Step 3. For each run t <E> T', replace any don't care entry by an
    // arbitrarily
    // selected value of the corresponding parameter. Instead, one
    // could also select a value that maximizes the number of
    // higher-oder tuples such as triples.

    T$ = replaceDontCareValues(info, T$);
    printTestRunSet("T'", T$);
    int pos = 0;
    for (IPOTestRun r : T) {
      T$.add(pos, r);
      pos++;
    }
    printTestRunSet("VG", T$);
    return T$;
  }
}

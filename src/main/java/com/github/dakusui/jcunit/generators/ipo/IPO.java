package com.github.dakusui.jcunit.generators.ipo;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides an implementation of the algorithm described in the book
 * "Foundations of Software Testing: Fundamental Algorithms and Techniques".
 * 
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
  static final Object DC = new Object();

  /**
   * The test space.
   */
  TestSpace space;

  /**
   * Creates an object of this class.
   * 
   * @param space
   *          the definition of the test space in which the test runs are
   *          executed.
   */
  public IPO(TestSpace space) {
    this.space = space;
  }

  /*
   * Input: (a) n >= 2: Number of input parameters and (b) number of values for
   * each parameter Output: CA: A set of parameter combinations such that all
   * pairs of values are covers at least once.
   */
  public TestRunSet ipo() {
    TestRunSet CA;
    int n = this.space.numDomains();
    assert n >= 2;

    // //
    // Step 1. [Initialize] Generate all pairs for parameters F1 and F2.
    // Set T = {(r,s) | for all r <E> D(f1) and s <E> D(F2)}.
    // # <E> means 'exists in'.
    TestRunSet T = new TestRunSet(2);
    int Fi = 1;
    for (Object vi : this.space.domainOf(1)) {
      int Fj = 2;
      for (Object vj : this.space.domainOf(2)) {
        TestRun cur = new TestRun(2);
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
      return CA;
    }
    ;

    // //
    // Step 3. [Add remaining parameters] Repeat the following steps for
    // Fk, k = 3, 4, ..., n

    // 3.1 [Horizontal growth] Replace each partial run (v1, v2,
    // vk-1) <E> T with (v1, v2, vk-1, vk), where vk is a suitably
    // selected value of parameter Fk. More details of this
    // this step are found in procedure HG described in the
    // function 'hg'.

    for (int k = 3; k <= n; k++) {
      int Fk = k;
      T = hg(T, Fk);

      // 3.2 [Uncovered pairs] Compute the set U of all uncovered pairs
      // formed by pairing parameters Fi, 1 <= i <= k-1 and parameter
      // Fk.
      Set<ValuePair> U = computeSetU(T, Fk);
      // 3.3 [Vertical growth] If U is empty, then terminate this step,
      // else continue. For each uncovered pair u = (vj, vk) <E> U,
      // add a run (v1, v2, ..., vj,..., vk-1, vk) to T. Here, vj
      // and vk denote values of parameters Fj and Fk respectively.
      // More details of this step are found in procedure VG described
      // in the function 'vg'.
      if (U.isEmpty())
        continue;
      T = vg(T, U);
    }

    // //
    // Return the composed test set.
    CA = T;
    return CA;
  }

  /*
   * Assuming T covers all possible pairs with in Fi and Fj, where 1 <= i <= j
   * <= k -1, compute all the possible value pair whose one parameter is Fk.
   */
  private Set<ValuePair> computeSetU(TestRunSet T, int Fk) {
    Set<ValuePair> ret = new LinkedHashSet<ValuePair>();
    for (TestRun t : T) {
      for (int Fi = 1; Fi <= T.width; Fi++) {
        Object r = t.get(Fi);
        for (Object s : this.space.domainOf(Fk)) {
          if (Fi == Fk)
            continue;
          ValuePair p = new ValuePair(Fi, r, Fk, s);
          if (lookUp(lookUp(T, Fi, r), Fk, s).size() == 0)
            ret.add(p);
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
  private TestRunSet hg(TestRunSet R, int F) {
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
    int k_1 = R.width();
    Set<ValuePair> AP = new LinkedHashSet<ValuePair>();
    for (int i = 1; i <= k_1; i++) {
      int k = k_1 + 1;
      if (i != k) {
        for (Object r : space.domainOf(i)) {
          for (Object s : space.domainOf(k)) {
            ValuePair pair = new ValuePair(i, r, k, s);
            AP.add(pair);
          }
        }
      }
    }

    // Step 2.
    // Let T' = <0>. T' denotes the set of runs obtained by extending the runs
    // in T in the following steps:
    // # <0> is an empty set.
    TestRunSet ret /* T' in the book */= new TestRunSet(R.width() + 1);

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
      TestRun tj$ = R.getRun(j).grow();
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
        if (F == ii)
          continue;
        AP.remove(new ValuePair(ii, tj$.get(ii), F, space.value(F, j)));
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
      TestRun tj = R.getRun(j);
      if (tj.width() < F) {
        // F is at most width of tj + 1
        tj = tj.grow();
      }
      // 6.1 Let AP′ = <0>
      Set<ValuePair> AP$ = new HashSet<ValuePair>();
      Object v$ = space.value(F, 1);
      for (int k = 1; k <= space.domainOf(F).length; k++) {
        // and v' = l1.
        // 6.2 In this step, we find a value of F by which to extend run
        // tj. The value that adds the maximum pairwise coverage is
        // selected. Repeat the next two substeps for each v <E> D(F).
        Set<ValuePair> AP$$ = new HashSet<ValuePair>();
        // 6.2.1 AP'' = {(r,v)|, where r is a value in run tj}. Here
        // AP'' is the set of all new pairs formed when run tj is
        // extended by v.
        Object v = space.value(F, k);
        for (int Fi : R.coveredParameters()) {
          ValuePair p = new ValuePair(Fi, tj.get(Fi), F, v);
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
        if (AP$$.size() >= AP$.size()) {
          AP$ = AP$$;
          v$ = v;
        }
      }

      // 6.3 Let = extend(tj, v′). T′ = T′ ∪ .
      tj.set(F, v$);
      ret.add(tj);

      // 6.4 AP = AP − AP′.
      AP.removeAll(AP$);
    }
    printTestRunSet("HG-2", ret);
    return ret /* T' in the book */;
  }

  private void printTestRunSet(String message, TestRunSet testRunSet) {
    if (LOGGER.isDebugEnabled()) {
      System.err.println("*** " + message + " ***");
      System.err.println(testRunSet.toString());
      System.err.println("*** *** ***");
    }
  }

  /**
   * Returns a list of elements whose parameter <code>F</code>'s value is
   * <code>v</code>.
   * 
   * @param runList
   *          A list from which matching runs are searched.
   * @param F
   *          parameter ID.
   * @param v
   *          the value of F.
   * @return The list of test runs which satisfy the given condition.
   */
  public static List<TestRun> lookUp(List<TestRun> runList, int F, Object v) {
    List<TestRun> ret = new LinkedList<TestRun>();
    for (TestRun cur : runList) {
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
  private TestRunSet replaceDontCareValues(TestRunSet testRunSet) {
    for (TestRun run : testRunSet) {
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
          run.set(i, this.space.value(i, 1));
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
  private TestRunSet vg(TestRunSet T, Set<ValuePair> MP) {
    /*
     * D(F) = {l1, l2,..., Lq}, q >= 1 t1, t2,..., tm denote the m >= 1 runs in
     * T. (Ai.r, Bj.s) denotes a pair of values r and s that correspond to
     * parameters A and B, respectively. In run (v1, V2,..., Vi-1, *, vi+1,...,
     * vk), i < k, an "*" denotes a don't care value for parameter Fi. When
     * needed, we use dc instead of "*"
     */

    // Step 1. Let T' = <0>
    TestRunSet T$ = new TestRunSet(T.width());

    // Step 2. Add new tests to cover the uncovered pairs.
    // For each missing pair (Fi.r, Fk.s) <E> MP, 1 <= i < k,
    // repeat the next two substeps.
    for (ValuePair pair : MP) {
      // 2.1 If there exists a run
      // (v1, v2,..., vi-1, *, vi+1,..., vk-1, s) <E> T'
      // then replace it by the run
      // (v1, v2,..., vi-1, r, vi+1,..., vk-1, s)
      // and examine the next missing pair, else go to the next substep
      List<TestRun> runsWhoseAis_rAndBisDC = lookUp(lookUp(T$, pair.A, pair.r),
          pair.B, DC);
      if (runsWhoseAis_rAndBisDC.size() > 0) {
        // Since parameters are processed from left to right, this
        // pass will not be executed under usual conditions.
        runsWhoseAis_rAndBisDC.get(0).set(pair.B, pair.s);
      }
      List<TestRun> runsWhoseBis_sAndAisDC = lookUp(lookUp(T$, pair.A, DC),
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
        TestRun r = new TestRun(k);
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

    printTestRunSet("T'", T$);
    T$ = replaceDontCareValues(T$);
    int pos = 0;
    for (TestRun r : T) {
      T$.add(pos, r);
      pos++;
    }
    printTestRunSet("VG", T$);
    return T$;
  }
}

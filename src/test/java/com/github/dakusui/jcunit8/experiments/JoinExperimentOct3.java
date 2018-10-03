

package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.List;

/**
 * -da -Xms14336m -Xmx14336m
 */
public class JoinExperimentOct3 {
  @Rule
  public TestName testName = new TestName();

  private static final boolean DEBUG = false;

  @Before
  public void before() {
    //warm up
    for (int i = 0; i < 10; i++)
      T$JOIN("warmup", 0, 0, 3);
    System.gc();
  }

  @Test
  public void T$s0_n3$() {
    long before = System.currentTimeMillis();
    List<Tuple> result = T$JOIN("test", 0, 0, 3);
    printResult(result, System.currentTimeMillis() - before);
  }

  @Test
  public void T$s1_n3$() {
    long before = System.currentTimeMillis();
    List<Tuple> result = T$JOIN("test", 0, 1, 3);
    printResult(result, System.currentTimeMillis() - before);
  }

  @Test
  public void T$s2_n3$() {
    long before = System.currentTimeMillis();
    List<Tuple> result = T$JOIN("test", 0, 2, 3);
    printResult(result, System.currentTimeMillis() - before);
  }

  @Test
  public void T$s3_n3$() {
    long before = System.currentTimeMillis();
    List<Tuple> result = T$JOIN("test", 0, 3, 3);
    printResult(result, System.currentTimeMillis() - before);
  }

  @Test
  public void T$s0_n4$() {
    long before = System.currentTimeMillis();
    List<Tuple> result = T$JOIN("test", 0, 0, 4);
    printResult(result, System.currentTimeMillis() - before);
  }

  @Test
  public void T$s1_n4$() {
    long before = System.currentTimeMillis();
    List<Tuple> result = T$JOIN("test", 0, 1, 4);
    printResult(result, System.currentTimeMillis() - before);
  }

  @Test
  public void T$s2_n4$() {
    long before = System.currentTimeMillis();
    List<Tuple> result = T$JOIN("test", 0, 2, 4);
    printResult(result, System.currentTimeMillis() - before);
  }

  @Test
  public void T$s3_n4$() {
    long before = System.currentTimeMillis();
    List<Tuple> result = T$JOIN("test", 0, 3, 4);
    printResult(result, System.currentTimeMillis() - before);
  }

  @Test
  public void T$s0_n5$() {
    long before = System.currentTimeMillis();
    List<Tuple> result = T$JOIN("test", 0, 0, 5);
    printResult(result, System.currentTimeMillis() - before);
  }

  @Test
  public void T$s1_n5$() {
    long before = System.currentTimeMillis();
    List<Tuple> result = T$JOIN("test", 0, 1, 5);
    printResult(result, System.currentTimeMillis() - before);
  }

  @Test
  public void T$s2_n5$() {
    long before = System.currentTimeMillis();
    List<Tuple> result = T$JOIN("test", 0, 2, 5);
    printResult(result, System.currentTimeMillis() - before);
  }


  private List<Tuple> T$JOIN(String prefix, int i, int s, int n) {
    if (s == 0)
      return testSuiteForComponent(String.format("%s:s=%s[%s]", prefix, s, i));
    String f = prefix + ":s=%s[%s]";
    if (DEBUG)
      System.out.println(String.format("i=%s,s=%s,j=%s/%s", i, s, 0, n));
    List<Tuple> ret = T$JOIN(String.format(f, s, 0), 0, s - 1, n);
    for (int j = 1; j < n; j++) {
      if (DEBUG)
        System.out.println(String.format("i=%s,s=%s,j=%s/%s", i, s, j, n));
      ret = join(ret, T$JOIN(String.format(f, s, j), j, s - 1, n));
    }
    return ret;
  }

  private List<Tuple> testSuiteForComponent(String prefix) {
    return JoinDataSet.load(2, 10, integer -> String.format("%s%03d", prefix, integer));
  }

  private List<Tuple> join(List<Tuple> lhs, List<Tuple> rhs) {
    return CoveringArrayGenerationUtils.join(lhs, rhs, 2);
    //return join(lhs, rhs, 2);
  }

  private void printResult(List<Tuple> result, long duration) {
    printTestInfo(this.testName);
    if (DEBUG)
      result.forEach(System.out::println);
    System.out.println(String.format("  time=%s[msec]", duration));
    System.out.println("  size=" + result.size());
    System.out.println("  width=" + result.get(0).size());
    System.out.println("---");
  }

  private void printTestInfo(TestName testName) {
    System.out.println(testName.getMethodName());
  }
}
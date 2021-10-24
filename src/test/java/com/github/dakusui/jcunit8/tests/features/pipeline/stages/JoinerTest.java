package com.github.dakusui.jcunit8.tests.features.pipeline.stages;

import com.github.dakusui.jcunit.core.tuples.KeyValuePairs;
import com.github.dakusui.jcunit.core.tuples.Row;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.ConfigFactory;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.testsuite.SchemafulRowSet;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;


@RunWith(Enclosed.class)
public class JoinerTest {
  static abstract class Base {
    @Test
    public void testLHSd1RHSd1() {
      List<Row> lhs = asList(
          KeyValuePairs.builder().put("l1", "v11").buildRow(),
          KeyValuePairs.builder().put("l1", "v12").buildRow());
      List<Row> rhs = asList(
          KeyValuePairs.builder().put("r1", "v11").buildRow(),
          KeyValuePairs.builder().put("r1", "v12").buildRow());
      executeTest(lhs, rhs);
    }

    @Test
    public void testLHSd2RHSd2() {
      List<Row> lhs = asList(
          KeyValuePairs.builder().put("l1", "v11").put("l2", "v21").buildRow(),
          KeyValuePairs.builder().put("l1", "v12").put("l2", "v22").buildRow());
      List<Row> rhs = asList(
          KeyValuePairs.builder().put("r1", "v21").put("r2", "v21").buildRow(),
          KeyValuePairs.builder().put("r1", "v22").put("r2", "v22").buildRow());
      executeTest(lhs, rhs);
    }

    @Test
    public void testLHSd3RHSd3() {
      List<Row> lhs = asList(
          KeyValuePairs.builder().put("l1", "v11").put("l2", "v21").put("l3", "v31").buildRow(),
          KeyValuePairs.builder().put("l1", "v12").put("l2", "v22").put("l3", "v32").buildRow());
      List<Row> rhs = asList(
          KeyValuePairs.builder().put("r1", "v11").put("r2", "v21").put("r3", "v31").buildRow(),
          KeyValuePairs.builder().put("r1", "v12").put("r2", "v22").put("r3", "v32").buildRow());
      executeTest(lhs, rhs);
    }

    private void executeTest(List<Row> lhs, List<Row> rhs) {
      Requirement requirement = new ConfigFactory.Default().create().getRequirement();
      Function<Requirement, Joiner> joinerFactory = joinerFactory();

      List<Row> joined = performJoin(lhs, rhs, requirement, joinerFactory);
      validateJoinedArray(joined, lhs, rhs);
    }

    abstract Function<Requirement, Joiner> joinerFactory();
  }

  public static class Standadrd extends Base {
    Function<Requirement, Joiner> joinerFactory() {
      return Joiner.Standard::new;
    }
  }

  public static class WeakenProduct extends Base {
    Function<Requirement, Joiner> joinerFactory() {
      return Joiner.WeakenProduct::new;
    }
  }

  public static class WeakenProduct2 extends Base {
    Function<Requirement, Joiner> joinerFactory() {
      return Joiner.WeakenProduct2::new;
    }
  }


  private static List<Row> performJoin(List<Row> lhs, List<Row> rhs, Requirement requirement1, Function<Requirement, Joiner> joinerFactory) {
    Joiner joiner = joinerFactory.apply(requirement1);
    return performJoin(joiner, lhs, rhs);
  }

  private static List<Row> performJoin(Joiner joiner, List<Row> lhs, List<Row> rhs) {
    return joiner.apply(
        SchemafulRowSet.fromRows(lhs),
        SchemafulRowSet.fromRows(rhs));
  }

  private static void validateJoinedArray(List<Row> joined, List<Row> lhs, List<Row> rhs) {
    assertThat(joined, not(nullValue()));
    if (lhs.isEmpty() || rhs.isEmpty()) {
      assertThat(joined.isEmpty(), CoreMatchers.is(true));
    }
    Stream.concat(lhs.stream(), rhs.stream()).filter(t -> joined.stream().noneMatch(j -> containsAll(j, t))).findAny()
        .ifPresent(t -> {
          throw new AssertionError("Tuple: " + t + " was not found in the 'join' operation result.: " + joined);
        });
  }

  private static boolean containsAll(KeyValuePairs j, KeyValuePairs t) {
    return t.isSubtupleOf(j);
  }
}

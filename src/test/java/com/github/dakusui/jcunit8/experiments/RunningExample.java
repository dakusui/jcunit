package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.pipeline.stages.joiners.StandardJoiner;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import org.junit.Test;

import java.util.Arrays;

public class RunningExample {
  /**
   * <pre>
   * +---+---------+     +---+---------+
   * |No.| Column  |     |No.| Column  |
   * |   |A1 A2 A3 |     |   |B1 B2 B3 |
   * +---|---------+     +---|---------+
   * |  1| 1  1  1 |     |  1| 1  1  1 |
   * |  2| 1  2  2 |     |  2| 1  2  2 |
   * |  3| 2  1  2 |     |  3| 2  1  2 |
   * |  4| 2  2  1 |     |  4| 2  2  1 |
   * +---+---------+     +---+---------+
   * </pre>
   */
  @Test
  public void run() {
    createJoiner().apply(
        SchemafulTupleSet.fromTuples(
            Arrays.asList(
                Tuple.builder().put("A1", 1).put("A2", 1).put("A3", 1).build(),
                Tuple.builder().put("A1", 1).put("A2", 2).put("A3", 2).build(),
                Tuple.builder().put("A1", 2).put("A2", 1).put("A3", 2).build(),
                Tuple.builder().put("A1", 2).put("A2", 2).put("A3", 1).build()
            )),
        SchemafulTupleSet.fromTuples(
            Arrays.asList(
                Tuple.builder().put("B1", 1).put("B2", 1).put("B3", 1).build(),
                Tuple.builder().put("B1", 1).put("B2", 2).put("B3", 2).build(),
                Tuple.builder().put("B1", 2).put("B2", 1).put("B3", 2).build(),
                Tuple.builder().put("B1", 2).put("B2", 2).put("B3", 1).build()
            ))).forEach(System.out::println);
  }

  @Test
  public void generateDirect2$90_() {

  }

  private Joiner createJoiner() {
    return new StandardJoiner(new Requirement.Builder().withStrength(2).build());
  }
}

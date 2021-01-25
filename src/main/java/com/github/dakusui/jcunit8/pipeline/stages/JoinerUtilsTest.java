package com.github.dakusui.jcunit8.pipeline.stages;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.pcond.functions.Printables;
import org.junit.Test;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.github.dakusui.crest.Crest.asListOf;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.jcunit8.pipeline.stages.Joiner.JoinerUtils.cartesianProduct;
import static com.github.dakusui.pcond.functions.Functions.size;
import static com.github.dakusui.pcond.functions.Predicates.equalTo;
import static com.github.dakusui.pcond.functions.Predicates.transform;
import static java.util.Arrays.asList;

public class JoinerUtilsTest {
  Joiner.WeakenProduct weakenProduct = new Joiner.WeakenProduct(new Requirement.Builder().build());

  @Test
  public void testCartesianProduct_1() {
    assertThat(
        cartesianProduct(
            asList(
                Tuple.builder().put("L0", "A").put("L1", "A").build(),
                Tuple.builder().put("L0", "B").put("L1", "B").build()
            ), asList(
                Tuple.builder().put("R0", "A").put("R1", "A").build(),
                Tuple.builder().put("R0", "B").put("R1", "B").build()
            )),
        asListOf(Tuple.class).check(size(), equalTo(4)).$()
    );
  }


  @Test
  public void testTupletsCoveredBy_1() {
    assertThat(
        weakenProduct.tupletsCoveredBy(SchemafulTupleSet.fromTuples(
            asList(
                Tuple.builder().put("L0", "A").put("L1", "A").build(),
                Tuple.builder().put("L0", "B").put("L1", "B").build())), 1),
        asListOf(Tuple.class)
            .allMatch(transform(keySet().andThen(size())).check(equalTo(1)))
            .check(size(), equalTo(4))
            .$()
    );
  }

  private static Function<Tuple, Set<String>> keySet() {
    return Printables.function("keySet", Map::keySet);
  }

  @Test
  public void testTupletsCoveredBy_2() {
    Set<Tuple> firstResult = weakenProduct.tupletsCoveredBy(SchemafulTupleSet.fromTuples(
        asList(
            Tuple.builder().put("L0", "A").put("L1", "A").build(),
            Tuple.builder().put("L0", "B").put("L1", "B").build())), 1);
    assertThat(
        weakenProduct.tupletsCoveredBy(SchemafulTupleSet.fromTuples(
            asList(
                Tuple.builder().put("L0", "A").put("L1", "A").build(),
                Tuple.builder().put("L0", "B").put("L1", "B").build())), 1),
        asListOf(Tuple.class)
            .allMatch(transform(keySet().andThen(size())).check(equalTo(1)))
            .check(size(), equalTo(4))
            .containsExactly(firstResult)
            .$());
  }

  @Test
  public void testTupletsCoveredBy_3() {
    Set<Tuple> firstResult = weakenProduct.tupletsCoveredBy(SchemafulTupleSet.fromTuples(
        asList(
            Tuple.builder().put("L0", "A").put("L1", "A").put("L2", "A").build(),
            Tuple.builder().put("L0", "B").put("L1", "B").put("L2", "B").build())), 2);
    System.out.println("----");
    assertThat(
        weakenProduct.tupletsCoveredBy(SchemafulTupleSet.fromTuples(
            asList(
                Tuple.builder().put("L0", "A").put("L1", "A").put("L2", "A").build(),
                Tuple.builder().put("L0", "B").put("L1", "B").put("L2", "B").build())), 2),
        asListOf(Tuple.class)
            .allMatch(transform(keySet().andThen(size())).check(equalTo(2)))
            .check(size(), equalTo(12))
            .containsExactly(firstResult)
            .$()
    );
  }
}

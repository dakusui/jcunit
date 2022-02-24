package com.github.dakusui.jcunit8.testsuite;

import com.github.dakusui.jcunit.core.tuples.AArray;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit8.core.TupleSet;
import com.github.dakusui.jcunit8.exceptions.FrameworkException;
import com.github.dakusui.pcond.functions.Functions;

import java.util.*;

import static com.github.dakusui.jcunit8.pipeline.PipelineException.checkIfStrengthIsInRange;
import static com.github.dakusui.pcond.Assertions.that;
import static com.github.dakusui.pcond.functions.Functions.chain;
import static com.github.dakusui.pcond.functions.Predicates.*;
import static java.util.Collections.unmodifiableList;

/**
 * @formatter:off
 * A list of tuples all of whose entries have the same attribute names. An implementation
 * of this interface must also guarantee that it doesn't have the same element.
 *
 * [ditaa]
 * ----
 * +-----+     +-----+
 * |hello|<>-->|world|
 * +-----+     +-----+
 * ----
 * @formatter:on
 */
public interface SchemafulAArraySet extends List<AArray> {
  static SchemafulAArraySet fromRows(List<AArray> rows) {
    Objects.requireNonNull(rows);
    FrameworkException.check(rows, r -> !r.isEmpty());
    return new Builder(new ArrayList<>(rows.get(0).keySet()))
        .addAll(rows)
        .build();
  }

  static SchemafulAArraySet empty(List<String> attributeNames) {
    return new Builder(attributeNames).build();
  }

  List<String> getAttributeNames();

  int width();

  /**
   * Returns all t-way tuples in this {@code SchemafulTupleSet} where t is {@code strength}.
   *
   * @param strength Strength of t-way tuples to be returned.
   * @return A set of sub-tuples of this.
   */
  TupleSet subtuplesOf(int strength);

  class Builder {
    private final List<String> attributeNames;
    private final List<AArray> rows;

    public Builder(List<String> attributeNames) {
      assert that(attributeNames, allOf(
          isNotNull(),
          transform(Functions.size()).check(equalTo(toSet(attributeNames).size()))
      ));
      this.attributeNames = unmodifiableList(attributeNames);
      this.rows = new LinkedList<>();
    }

    private static HashSet<String> toSet(final List<String> list) {
      return new HashSet<String>() {{
        this.addAll(list);
      }};
    }

    public Builder add(AArray row) {
      ////
      // Make sure all the tuples in this suite object have the same set of attribute
      // names.
      assert that(row, allOf(
          isNotNull(),
          transform(chain("keySet")).check(isEqualTo(toSet(attributeNames)))));
      this.rows.add(row);
      return this;
    }

    public Builder addAll(List<AArray> tuples) {
      tuples.forEach(this::add);
      return this;
    }

    public SchemafulAArraySet build() {
      class Impl extends AbstractList<AArray> implements SchemafulAArraySet {
        private final List<AArray> rows;
        private final List<String> attributeNames;

        private Impl(List<String> attributeNames, List<AArray> rows) {
          this.rows = rows;
          this.attributeNames = unmodifiableList(attributeNames);
        }

        @Override
        public AArray get(int index) {
          return rows.get(index);
        }

        @Override
        public int size() {
          return rows.size();
        }

        @Override
        public List<String> getAttributeNames() {
          return attributeNames;
        }

        @Override
        public int width() {
          return getAttributeNames().size();
        }

        @Override
        public TupleSet subtuplesOf(int strength) {
          checkIfStrengthIsInRange(strength, attributeNames);
          TupleSet.Builder builder = new TupleSet.Builder();
          for (AArray each : this) {
            builder.addAll(TupleUtils.subtuplesOf(each, strength));
          }
          return builder.build();
        }
      }
      return new Impl(
          new ArrayList<>(this.attributeNames),
          this.rows);
    }
  }

}

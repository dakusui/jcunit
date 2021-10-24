package com.github.dakusui.jcunit8.testsuite;

import com.github.dakusui.jcunit.core.tuples.KeyValuePairs;
import com.github.dakusui.jcunit.core.tuples.Row;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.exceptions.FrameworkException;

import java.util.*;

import static com.github.dakusui.jcunit8.pipeline.PipelineException.checkIfStrengthIsInRange;

/**
 * A list of tuples all of whose entries have the same attribute names. An implementation
 * of this interface must also guarantee that it doesn't have the same element.
 */
public interface SchemafulRowSet extends List<Row> {
  List<String> getAttributeNames();
  int width();

  /**
   * Returns all t-way tuples in this {@code SchemafulTupleSet} where t is {@code strength}.
   *
   * @param strength Strength of t-way tuples to be returned.
   * @return A set of sub-tuples of this.
   */
  TupleSet subtuplesOf(int strength);

  static SchemafulRowSet fromRows(List<Row> rows) {
    Objects.requireNonNull(rows);
    FrameworkException.check(rows, tuples -> !tuples.isEmpty());
    return new Builder(new ArrayList<>(rows.get(0).keySet()))
        .addAll(rows)
        .build();
  }

  static SchemafulRowSet empty(List<String> attributeNames) {
    return new Builder(attributeNames).build();
  }

  class Builder {
    private final LinkedHashSet<String> attributeNames;
    private final List<Row>   tuples;

    public Builder(List<String> attributeNames) {
      this.attributeNames = new LinkedHashSet<String>() {{
        addAll(attributeNames);
      }};
      this.tuples = new LinkedList<>();
    }

    public Builder add(Row row) {
      ////
      // Make sure all the tuples in this suite object have the same set of attribute
      // names.
      FrameworkException.check(row, (KeyValuePairs t) -> attributeNames.equals(row.keySet()));
      this.tuples.add(row);
      return this;
    }

    public Builder addAll(List<Row> rows) {
      rows.forEach(this::add);
      return this;
    }

    public SchemafulRowSet build() {
      class Impl extends AbstractList<Row> implements SchemafulRowSet {
        private final List<Row> tuples;
        private final List<String>        attributeNames;

        private Impl(List<String> attributeNames, List<Row> tuples) {
          this.tuples = tuples;
          this.attributeNames = Collections.unmodifiableList(attributeNames);
        }

        @Override
        public Row get(int index) {
          return tuples.get(index);
        }

        @Override
        public int size() {
          return tuples.size();
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
          for (KeyValuePairs each : this) {
            builder.addAll(TupleUtils.subtuplesOf(each, strength));
          }
          return builder.build();
        }
      }
      return new Impl(
          new ArrayList<>(this.attributeNames),
          this.tuples);
    }
  }

}

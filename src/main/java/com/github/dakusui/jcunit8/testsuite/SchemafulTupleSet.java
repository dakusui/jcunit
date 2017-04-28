package com.github.dakusui.jcunit8.testsuite;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit8.exceptions.FrameworkException;

import java.util.*;

import static com.github.dakusui.jcunit8.pipeline.PipelineException.checkIfStrengthIsInRange;
import static java.util.stream.Collectors.toList;

/**
 * A list of tuples all of whose entries have the same attribute names. An implementation
 * of this interface must also guarantee that it doesn't have the same element.
 */
public interface SchemafulTupleSet extends List<Tuple> {
  List<String> getAttributeNames();

  /**
   * Returns all t-way tuples in this {@code SchemafulTupleSet} where t is {@code strength}.
   *
   * @param strength Strength of t-way tuples to be returned.
   */
  TupleSet subtuplesOf(int strength);

  static SchemafulTupleSet fromTuples(List<Tuple> tuples_) {
    Objects.requireNonNull(tuples_);
    FrameworkException.check(tuples_, tuples -> !tuples.isEmpty());
    return new Builder(tuples_.get(0).keySet().stream().collect(toList()))
        .addAll(tuples_)
        .build();
  }

  class Builder {
    private final LinkedHashSet<String> attributeNames;
    private final List<Tuple>           tuples;

    public Builder(List<String> attributeNames) {
      this.attributeNames = new LinkedHashSet<String>() {{
        addAll(attributeNames);
      }};
      this.tuples = new LinkedList<>();
    }

    public Builder add(Tuple tuple) {
      ////
      // Make sure all the tuples in this suite object have the same set of attribute
      // names.
      FrameworkException.check(tuple, (Tuple t) -> attributeNames.equals(tuple.keySet()));
      this.tuples.add(tuple);
      return this;
    }

    public Builder addAll(List<Tuple> tuples) {
      tuples.forEach(this::add);
      return this;
    }

    public SchemafulTupleSet build() {
      class Impl extends AbstractList<Tuple> implements SchemafulTupleSet {
        private final List<Tuple>  tuples;
        private final List<String> attributeNames;

        private Impl(List<String> attributeNames, List<Tuple> tuples) {
          this.tuples = tuples;
          this.attributeNames = Collections.unmodifiableList(attributeNames);
        }

        @Override
        public Tuple get(int index) {
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
        public TupleSet subtuplesOf(int strength) {
          checkIfStrengthIsInRange(strength, attributeNames);
          TupleSet.Builder builder = new TupleSet.Builder();
          for (Tuple each : this) {
            builder.addAll(TupleUtils.subtuplesOf(each, strength));
          }
          return builder.build();
        }
      }
      return new Impl(
          this.attributeNames.stream().collect(toList()),
          this.tuples);
    }
  }

}

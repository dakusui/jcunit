package com.github.jcunit.core;

import com.github.dakusui.combinatoradix.CartesianEnumeratorAdaptor;
import com.github.dakusui.combinatoradix.Domains;
import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.factorspace.Factor;

import java.util.AbstractList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamableTupleCartesianator extends CartesianEnumeratorAdaptor<Tuple, String, Object> {
  public StreamableTupleCartesianator(List<Factor> factors) {
    super(buildDomains(factors));
  }

  public Cursor<Tuple> cursor(Tuple at) {
    return new Cursor.ForTuple(indexOf(at), this);
  }

  public Stream<Tuple> stream() {
    return StreamSupport.stream(this.spliterator(), false);
  }

  @Override
  protected Tuple createMap() {
    return new Tuple.Builder().build();
  }

  private static Domains<String, Object> buildDomains(List<Factor> factors) {
    Domains.Builder<String, Object> builder = new Domains.Builder<>();
    factors.forEach(factor -> builder.addDomain(factor.getName(), factor.getLevels().toArray()));
    return builder.build();
  }
}

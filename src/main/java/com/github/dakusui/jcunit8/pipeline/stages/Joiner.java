package com.github.dakusui.jcunit8.pipeline.stages;

import com.github.dakusui.jcunit8.exceptions.FrameworkException;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;

import java.util.Collections;
import java.util.LinkedList;
import java.util.function.BinaryOperator;

public interface Joiner extends BinaryOperator<SchemafulTupleSet> {
  abstract class Base implements Joiner {
    @Override
    public SchemafulTupleSet apply(SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
      FrameworkException.checkCondition(Collections.disjoint(lhs.getAttributeNames(), rhs.getAttributeNames()));
      if (lhs.isEmpty() || rhs.isEmpty())
        return emptyTupleSet(lhs, rhs);
      if (lhs.size() > rhs.size())
        return doJoin(lhs, rhs);
      return doJoin(rhs, lhs);
    }

    private SchemafulTupleSet emptyTupleSet(SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
      return SchemafulTupleSet.empty(new LinkedList<String>() {{
        addAll(lhs.getAttributeNames());
        addAll(rhs.getAttributeNames());
      }});
    }

    protected abstract SchemafulTupleSet doJoin(SchemafulTupleSet lhs, SchemafulTupleSet rhs);
  }

}



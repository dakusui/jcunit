package com.github.dakusui.jcunit.constraint;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public interface LabeledTestCase {
  public static class Builder {
    private List<Serializable> labels = new LinkedList<Serializable>();
    private Tuple testCase;

    public Builder addLabels(Serializable... labels) {
      this.labels.addAll(Arrays.asList(labels));
      return this;
    }

    public Builder setTestCase(Tuple testCase) {
      this.testCase = testCase;
      return this;
    }

    public LabeledTestCase build() {
      final Tuple testCase = Utils.checknotnull(this.testCase);
      final List<Serializable> labels = Collections.unmodifiableList(Utils.checknotnull(this.labels, "This builder can't be used anymore"));
      ////
      // In order to prevent users to modify the backing labels list,
      // assign a null to 'labels' field.
      this.labels = new LinkedList<Serializable>(this.labels);
      return new LabeledTestCase() {
        List<Serializable> labels = Builder.this.labels;
        @Override
        public List<Serializable> getLabels() {
          return labels;
        }
        @Override
        public Tuple getTestCase() {
          return testCase;
        }
        @Override public boolean isSet(Serializable label) {
          return this.labels.contains(label);
        }
      };
    }
  }
  public List<Serializable> getLabels();
  public Tuple getTestCase();
  public boolean isSet(Serializable label);
}

package com.github.dakusui.jcunit.core;

import com.github.dakusui.enumerator.tuple.AttrValue;
import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 */
public class TestCaseUtils {
	public static LabeledTestCase createLabeledTestCase(List<Serializable> labels, Tuple testCase) {
		Utils.checknotnull(labels);
		Utils.checknotnull(testCase);
		return new LabeledTestCase.Builder().addLabels((Serializable[])labels.toArray(new Serializable[labels.size()])).setTestCase(testCase).build();
	}

	public static List<Serializable> labels(Serializable... labels) {
		return Arrays.asList(labels);
	}

	public static AttrValue<String, Object> factor(String name, Object level) {
    return new AttrValue<String, Object>(name, level);
  }

	public static Tuple newTestCase(AttrValue<String, Object>... attrs) {
	  Tuple.Builder b = new Tuple.Builder();
	  for (AttrValue<String, Object> attrValue : attrs) {
	    b.put(attrValue.attr(), attrValue.value());
	  }
	  return b.build();
	}
}

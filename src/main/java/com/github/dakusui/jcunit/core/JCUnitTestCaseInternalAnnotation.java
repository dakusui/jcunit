package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

public class JCUnitTestCaseInternalAnnotation implements Annotation {

  private final JCUnitTestCaseType type;
  private final List<Serializable> labels;
  private final int                id;
  private Tuple testCase;


  public JCUnitTestCaseInternalAnnotation(int id, JCUnitTestCaseType type, List<Serializable> labels, Tuple testCase) {
    Utils.checknotnull(type);
    Utils.checknotnull(labels);
    this.id = id;
    this.type = type;
    this.labels = labels;
    this.testCase = testCase;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
		return this.getClass();
	}

	public int getId() {
		return this.id;
	}

	public JCUnitTestCaseType getTestCaseType() {
		return this.type;
	}

	public List<Serializable> getLabels() {
		return Collections.unmodifiableList(this.labels);
	}

  public Tuple getTestCase() {
    return testCase;
  }
}

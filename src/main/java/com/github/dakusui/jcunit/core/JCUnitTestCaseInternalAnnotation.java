package com.github.dakusui.jcunit.core;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

public class JCUnitTestCaseInternalAnnotation implements Annotation {

	private final JCUnitTestCaseType type;
	private final List<Serializable> labels;
	private final int id;


	public JCUnitTestCaseInternalAnnotation(int id, JCUnitTestCaseType type, List<Serializable> labels) {
		Utils.checknotnull(type);
		Utils.checknotnull(labels);
		this.id = id;
		this.type = type;
		this.labels = labels;
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
}

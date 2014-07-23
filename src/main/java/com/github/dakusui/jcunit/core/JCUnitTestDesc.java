package com.github.dakusui.jcunit.core;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JCUnitTestDesc extends TestWatcher {
	private String testName;
	private int id;
	private List<Serializable> labels;
	private JCUnitTestCaseType type;

	@Override
	protected void starting(Description d) {
		JCUnitTestCaseInternalAnnotation ann = d.getAnnotation(JCUnitTestCaseInternalAnnotation.class);
		Utils.checknotnull(ann, "This class(%s) should be used with classes annotated @RunWith(%s.class)", this.getClass(), JCUnit.class.getClass());
		testName = d.getMethodName();
		this.id = ann.getId();
		this.type = ann.getTestCaseType();
		this.labels = ann.getLabels();
	}

	public JCUnitTestCaseType getTestCaseType() {
		return this.type;
	}


	public String getTestName() {
		return this.testName;
	}

	public List<Serializable> getLabels() {
		return Collections.unmodifiableList(this.labels);
	}

	public <T extends Serializable> Iterable<T> getLabels(Class<T> clazz) {
		Utils.checknotnull(clazz);
		List<T> ret = new ArrayList<T>(this.labels.size());
		for (Serializable l : this.labels) {
			if (clazz.isAssignableFrom(l.getClass())) {
				ret.add((T) l);
			}
		}
		return ret;
	}
}

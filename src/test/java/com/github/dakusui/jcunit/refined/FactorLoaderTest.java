package com.github.dakusui.jcunit.refined;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.FactorLoader;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class FactorLoaderTest {

	private final TestFactors testFactors = new TestFactors();

	@Test
	public void loadFactor() throws NoSuchFieldException {
		FactorLoader factorLoader = new FactorLoader(this.testFactors.getClass().getField("intField"));
		Factor f = factorLoader.getFactor();
		assertArrayEquals(new Object[]{1, 0, -1, 100, -100, Integer.MAX_VALUE,
				Integer.MIN_VALUE}, f.levels.toArray());
	}

	@Test
	public void loadFactor2() throws NoSuchFieldException {
		FactorLoader factorLoader = new FactorLoader(this.testFactors.getClass().getField("intField2"));
		Factor f = factorLoader.getFactor();
		assertArrayEquals(new Object[]{1, 2, 3}, f.levels.toArray());
	}

	@Test
	public void invalidFactorsIncompatibleLevels() throws Exception {
		FactorLoader factorLoader = new FactorLoader(this.testFactors.getClass().getField("intField3"));
		FactorLoader.ValidationResult result = factorLoader.validate();
		System.out.println(result.getErrorMessage());
	}

	@Test
	public void validateValidField1() throws Exception {
		FactorLoader factorLoader = new FactorLoader(this.testFactors.getClass().getField("intField"));
		FactorLoader.ValidationResult result = factorLoader.validate();
		assertTrue(result.isValid());
	}

	@Test
	public void validateValidField2() throws Exception {
		FactorLoader factorLoader = new FactorLoader(this.testFactors.getClass().getField("intField2"));
		FactorLoader.ValidationResult result = factorLoader.validate();
		assertTrue(result.isValid());
	}

	@Test
	public void validateInvalidField1() throws Exception {
		Field f = this.testFactors.getClass().getField("intField3");
		FactorLoader factorLoader = new FactorLoader(f);
		FactorLoader.ValidationResult result = factorLoader.validate();
		assertThat(result.isValid(), is(false));
		assertThat(result.getErrorMessage(), containsString("can't be assigned"));
		assertThat(result.getErrorMessage(), containsString(f.getName()));
	}

	@Test
	public void validateInvalidField2() throws Exception {
		Field f = this.testFactors.getClass().getField("intField3");
		FactorLoader factorLoader = new FactorLoader(this.testFactors.getClass().getField("intField3"));
		try {
			Factor factor = factorLoader.getFactor();
			System.out.println(factor);
		} catch (FactorLoader.FactorFieldValidationException e) {
			FactorLoader.ValidationResult result = e.getValidationResult();
			assertThat(result.isValid(), is(false));
			assertThat(result.getErrorMessage(), containsString("can't be assigned"));
			assertThat(result.getErrorMessage(), containsString(f.getName()));
		}
	}
}

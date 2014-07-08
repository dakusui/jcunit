package com.github.dakusui.jcunit.core.annotations;

import com.github.dakusui.jcunit.core.factorfactories.DefaultFactorFactory;
import com.github.dakusui.jcunit.core.factorfactories.FactorFactory;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FactorField {
	public Levels value() default @Levels;
	public static @interface Levels {
		boolean[] booleanValues() default {true, false};
		byte[] byteValues() default {};
		char[] charValues() default {};
		short[] shortValues() default {};
		int[] intValues() default {1,2,3};
		long[] longValues() default {};
		float[] floatValues() default {};
		double[] doubleValues() default {};
		public Class<? extends FactorFactory> factory() default DefaultFactorFactory.class;
	}
}

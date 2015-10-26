package com.github.dakusui.jcunit.runners.theories;

import com.github.dakusui.jcunit.plugins.generators.IPO2TupleGenerator;
import com.github.dakusui.jcunit.plugins.generators.TupleGenerator;
import com.github.dakusui.jcunit.runners.standard.annotations.Generator;
import com.github.dakusui.jcunit.runners.standard.annotations.TupleGeneration;
import com.github.dakusui.jcunit.runners.standard.annotations.Value;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(TheoriesWithJCUnit.class)
@TupleGeneration(generator = @Generator(value = IPO2TupleGenerator.class, params = {@Value("3")}))
public class TheoriesExample {
	@DataPoints("posInt")
	public static int[] positiveIntegers() {
		return new int[] {
				1, 2, 3
		};
	}

	@DataPoints("negInt")
	public static int[] negativeIntegers() {
		return new int[] {
				-1, -2, -3
		};
	}

	@DataPoints("posLong")
	public static long[] posLongs() {
		return new long[] {
				100, 200, 300
		};
	}

	@DataPoints("negLong")
	public static long[] negLongs() {
		return new long[] {
				-100, -200, -300
		};
	}

	@Theory
	public void test1(
			@FromDataPoints("posInt") int a,
			@FromDataPoints("negInt") int b,
			@FromDataPoints("posLong") long c,
			@FromDataPoints("negLong") long d
			) throws Exception {
		System.out.printf("a=%s, b=%s, c=%d, d=%d%n", a, b, c, d);
	}

	@Theory
	public void test2(
			@FromDataPoints("posInt") int a,
			@FromDataPoints("negInt") int b
	) throws Exception {
		System.out.printf("a=%s, b=%s%n", a, b);
	}
}

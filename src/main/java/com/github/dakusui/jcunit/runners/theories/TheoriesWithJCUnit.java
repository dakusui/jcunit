package com.github.dakusui.jcunit.runners.theories;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.plugins.Plugin;
import com.github.dakusui.jcunit.plugins.constraintmanagers.ConstraintManager;
import com.github.dakusui.jcunit.plugins.generators.IPO2TupleGenerator;
import com.github.dakusui.jcunit.plugins.generators.TupleGenerator;
import com.github.dakusui.jcunit.runners.standard.annotations.TupleGeneration;
import com.github.dakusui.jcunit.runners.standard.annotations.Value;
import org.junit.Assert;
import org.junit.experimental.theories.*;
import org.junit.experimental.theories.internal.Assignments;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TheoriesWithJCUnit extends Theories {
	public TheoriesWithJCUnit(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	public Statement methodBlock(final FrameworkMethod method) {
		Factors.Builder factorsBuilder = new Factors.Builder();
		final TestClass testClass = getTestClass();
		Assignments assignments = Assignments.allUnassigned(method.getMethod(), testClass);
		try {
			int i = 0;
			while (!assignments.isComplete()) {
				List<PotentialAssignment> potentials = assignments.potentialsForNextUnassigned();
				FromDataPoints fromDataPoints = assignments.nextUnassigned().getAnnotation(FromDataPoints.class);
				String factorName = "nosourcename";
				if (fromDataPoints != null) {
					factorName = fromDataPoints.value();
				}
				////
				// Guarantee the factors names are generated in dictionary order.
				factorName = String.format("param%03d:%s", i + 1, factorName);
				Factor.Builder factorBuilder = new Factor.Builder(factorName);
				for (PotentialAssignment each : potentials) {
					factorBuilder.addLevel(each);
				}
				factorsBuilder.add(factorBuilder.build());
				assignments = assignments.assignNext(null);
				i++;
			}
		} catch (Throwable throwable) {
			Checks.rethrow(throwable);
		}
		final TupleGenerator tg = createTupleGenerator();
		tg.setFactors(factorsBuilder.build());
		tg.init();
		return new TheoryAnchor(method, testClass) {
			int successes = 0;
			List<AssumptionViolatedException> fInvalidParameters = new ArrayList<AssumptionViolatedException>();

			@Override public void evaluate() throws Throwable {
				for (int i = 0; i < tg.size(); i++) {
					runWithCompleteAssignment(tuple2assignments(method.getMethod(), testClass, tg.get(i)));
				}
				//if this test method is not annotated with Theory, then no successes is a valid case
				boolean hasTheoryAnnotation = method.getAnnotation(Theory.class) != null;
				if (successes == 0 && hasTheoryAnnotation) {
					Assert.fail("Never found parameters that satisfied method assumptions.  Violated assumptions: "
							+ fInvalidParameters);
				}
			}

			@Override protected void handleAssumptionViolation(AssumptionViolatedException e) {
				fInvalidParameters.add(e);
			}

			@Override protected void handleDataPointSuccess() {
				successes++;
			}
		};
	}

	protected ConstraintManager createConstraintManager() {
		return ConstraintManager.DEFAULT_CONSTRAINT_MANAGER;
	}

	protected TupleGenerator createTupleGenerator() {
		TupleGeneration ann = getTestClass().getAnnotation(TupleGeneration.class);
		TupleGenerator ret;
		if (ann != null) {
			ret = createTupleGenerator(ann);
		} else {
			ret = new IPO2TupleGenerator(2);
			ret.setConstraintManager(createConstraintManager());
		}
		return ret;
	}

	private TupleGenerator createTupleGenerator(TupleGeneration ann) {
		Value.Resolver resolver = new Value.Resolver();
		Plugin.Factory<TupleGenerator,Value> tgFactory = new Plugin.Factory(ann.generator().value(), resolver);
		TupleGenerator ret = Checks.cast(TupleGenerator.class, tgFactory.create(ann.generator().params()));
		ret.setConstraintManager(new Plugin.Factory<ConstraintManager, Value>((Class<ConstraintManager>)ann.constraint().value(), resolver).create());
		return ret;
	}

	private static Assignments tuple2assignments(Method method, TestClass testClass, Tuple t) {
		// Tuple generator generates dictionary order guaranteed tuples.
		Assignments ret = Assignments.allUnassigned(method, testClass);
		for (Object each : t.values()) {
			ret = ret.assignNext(Checks.cast(PotentialAssignment.class, each));
		}
		return ret;
	}
}

package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.FactorField;
import com.github.dakusui.jcunit.core.factor.FactorLoader;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.exceptions.JCUnitCheckedException;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.exceptions.JCUnitPluginException;
import com.github.dakusui.jcunit.generators.TestCaseGenerator;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class JCUnit extends Suite {
  private final ArrayList<Runner> runners = new ArrayList<Runner>();

  /**
   * Only called reflectively. Do not use programmatically.
   */
  public JCUnit(Class<?> klass) throws Throwable {
    super(klass, Collections.<Runner>emptyList());
    List<Tuple> parametersList = getTestCasesFor(getTestClass());
    for (int i = 0; i < parametersList.size(); i++) {
      runners.add(new JCUnitRunner(getTestClass().getJavaClass(),
          parametersList, i));
    }
  }

  /**
   * @param generatorClass A generator class to be used for <code>cut</code>
   * @param params         Parameters given to {@code init(String[])} method of a generator.
   * @param factors        Domain definitions for all the fields.
   */
  public static TestCaseGenerator newTestCaseGenerator(
      @SuppressWarnings("rawtypes")
      Class<? extends TestCaseGenerator> generatorClass,
      String[] params,
      Factors factors) {
    TestCaseGenerator ret;
    try {
      ret = generatorClass.newInstance();
      ret.init(params, factors);
    } catch (InstantiationException e) {
      throw new JCUnitPluginException(e.getMessage(), e);
    } catch (IllegalAccessException e) {
      throw new JCUnitPluginException(e.getMessage(), e);
    }
    return ret;
  }

  private static Generator getGeneratorAnnotation(Class<?> testClass) {
    Utils.checknotnull(testClass);
    Generator ret = testClass.getAnnotation(Generator.class);
    if (ret == null) {
      Class<?> superClass = testClass.getSuperclass();
      if (superClass == null) {
        return null;
      }
      return getGeneratorAnnotation(superClass);
    }
    return ret;
  }

  @Override
  protected List<Runner> getChildren() {
    return runners;
  }

  private List<Tuple> getTestCasesFor(TestClass klass) throws Throwable {
    Utils.checknotnull(klass);
    Class<?> testClass = klass.getJavaClass();
    Utils.checknotnull(testClass);
    Generator generatorAnn = getGeneratorAnnotation(testClass);
    Class<? extends TestCaseGenerator> generatorClass;
    String[] generatorParams;
    if (generatorAnn != null) {
      generatorClass = generatorAnn.value();
      if (generatorClass == null) {
        generatorClass = IPO2TestCaseGenerator.class;
      }
      generatorParams = generatorAnn.parameters();
      Utils.checknotnull(generatorParams);
    } else {
      generatorClass = IPO2TestCaseGenerator.class;
      generatorParams = new String[] { };

    }
    return this
        .composeTestArray(testClass, generatorClass, generatorParams);
  }

  /*
     * Composes the test array.
     */
  public List<Tuple> composeTestArray(
      Class<?> cut,
      @SuppressWarnings("rawtypes")
      Class<? extends TestCaseGenerator> generatorClass,
      String[] params)
      throws JCUnitCheckedException {
    if (generatorClass == null) {
      throw new NullPointerException();
    }
    // //
    // Initialize the factor levels for every '@FactorField' annotated field.
    Field[] fields = Utils.getAnnotatedFields(cut, FactorField.class);
    Factors.Builder factorsBuilder = new Factors.Builder();
    List<String> errors = new LinkedList<String>();
    for (Field f : fields) {
      FactorLoader factorLoader = new FactorLoader(f);
      FactorLoader.ValidationResult validationResult = factorLoader.validate();
      if (!validationResult.isValid()) {
        errors.add(f.getName() + ":" + validationResult.getErrorMessage());
      }
      Factor factor = factorLoader.getFactor();
      factorsBuilder.add(factor);
    }
    if (!errors.isEmpty()) {
      errors.add(0, "One or more factors failed to be initialized.");
      throw new JCUnitException(Utils.join("\n\t", errors.toArray()));
    }

    // //
    // Instantiates the test array generator.
    TestCaseGenerator testCaseGenerator = JCUnit
        .newTestCaseGenerator(generatorClass, params, factorsBuilder.build());

    // //
    // Compose an array to be returned to the caller.
    List<Tuple> ret = new ArrayList<Tuple>();
    for (Tuple testCase : testCaseGenerator) {
      ret.add(testCase);
    }
    return ret;
  }
}

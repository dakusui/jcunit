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
    List<Tuple> parametersList = getParametersList(getTestClass());
    for (int i = 0; i < parametersList.size(); i++) {
      runners.add(new JCUnitRunner(getTestClass().getJavaClass(),
          parametersList, i));
    }
  }

  /**
   * @param generatorClass A generator class to be used for <code>cut</code>
   * @param params         TODO
   * @param factors        Domain definitions for all the fields.
   */
  public static TestCaseGenerator newTestArrayGenerator(
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

  @SuppressWarnings("rawtypes")
  private static Class<? extends TestCaseGenerator> getTestArrayGeneratorClass(
      Class<?> cuf) {
    Generator an = cuf.getAnnotation(Generator.class);
    Class<? extends TestCaseGenerator> ret = an != null ? an.value() : null;
    if (ret != null) {
      return ret;
    } else {
      Class<?> superClass = cuf.getSuperclass();
      if (superClass == null) {
        return null;
      }
      return getTestArrayGeneratorClass(superClass);
    }
  }

  @Override
  protected List<Runner> getChildren() {
    return runners;
  }

  private List<Tuple> getParametersList(TestClass klass) throws Throwable {
    @SuppressWarnings("rawtypes")
    Class<? extends TestCaseGenerator> generatorClass = getTestArrayGeneratorClass(
        klass
            .getJavaClass()
    );
    if (generatorClass == null) {
      generatorClass = IPO2TestCaseGenerator.class;
    }
    Class<?> testClass = klass.getJavaClass();
    Generator generatorAnn = generatorClass.getAnnotation(Generator.class);
    return this
        .composeTestArray(testClass, generatorClass, generatorAnn.parameters());
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
      throw new JCUnitException(Utils.join("\n\t", errors.toArray()),
          null);
    }

    // //
    // Instantiates the test array generator.
    TestCaseGenerator testCaseGenerator = JCUnit
        .newTestArrayGenerator(generatorClass, params, factorsBuilder.build());

    // //
    // Compose an array to be returned to the caller.
    List<Tuple> ret = new ArrayList<Tuple>();
    for (Tuple testCase : testCaseGenerator) {
      ret.add(testCase);
    }
    return ret;
  }
}

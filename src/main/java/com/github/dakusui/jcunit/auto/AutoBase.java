package com.github.dakusui.jcunit.auto;

import java.io.File;
import java.lang.reflect.Field;

import com.github.dakusui.jcunit.auto.encoder.ObjectEncoder;
import com.github.dakusui.jcunit.auto.encoder.ObjectEncoders;
import com.github.dakusui.jcunit.core.SystemProperties;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;
import com.github.dakusui.lisj.func.BaseFunc;

/**
 * An abstract base class for 'automatic capture-replay based tests'.
 * 
 * Below are the list of valid parameters for subclasses of this class.
 * 
 * <ol>
 * <li>String testName: A name of a test.</li>
 * <li>Object obj: An object under the test.</li>
 * <li>String fieldName: A name of a field to be tested.</li>
 * </ol>
 * 
 * @author hiroshi
 */
public abstract class AutoBase extends BaseFunc {
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = -2402014565260025741L;

  /**
   * This function takes one and only one parameter, which is a name of a field.
   * 
   * @see com.github.dakusui.lisj.BaseForm#checkParams(java.lang.Object)
   */
  @Override
  protected Object checkParams(Object params) {
    super.checkParams(params);
    if (Basic.length(params) != 3)
      throw new IllegalArgumentException();
    Utils.checknull(Basic.get(params, 0));
    if (!(Basic.get(params, 0) instanceof String)) {
      throw new IllegalArgumentException();
    }
    Utils.checknull(Basic.get(params, 1));
    Utils.checknull(Basic.get(params, 2));
    return params;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  final protected FormResult evaluateLast(Context context,
      Object[] evaluatedParams, FormResult lastResult) throws JCUnitException,
      CUT {
    FormResult ret = lastResult;
    /*
     * We can use the first, second, and third elements without a check since
     * 'checkParams' method guarantees that the array has three and only three
     * elements.
     */
    String testName = (String) evaluatedParams[0];
    Object obj = evaluatedParams[1];
    String fieldName = evaluatedParams[2].toString();
    ret.value(autoBaseExec(testName, obj, fieldName));
    return ret;
  }

  abstract protected Object autoBaseExec(String testName, Object obj,
      String fieldName) throws JCUnitException;

  /**
   * Returns base directory to output the values of '@Out' annotated fields.
   * 
   * @return The base directory.
   */
  protected File baseDir() {
    return SystemProperties.jcunitBaseDir();
  }

  protected Field field(Class<?> clazz, String fieldName)
      throws JCUnitException {
    return Utils.getFieldFromClass(clazz, fieldName);
  }

  protected File fileForField(File baseDir, String testName, Field out) {
    // //
    // Since only fields in test class can be treated as 'output' fields,
    // we don't need to include its class name in the directory name.
    return new File(baseDir, testName + "/" + out.getName());
  }

  protected ObjectEncoder getObjectEncoder(Class<?> clazz) {
    ObjectEncoder ret = null;
    ret = ObjectEncoders.createXStreamEncoder();
    return ret;
  }
}
package com.github.dakusui.jcunit.compat.auto;

import com.github.dakusui.jcunit.exceptions.JCUnitCheckedException;

public class IsStored extends AutoBase {

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 9156779945969062675L;

  @Override
  protected Object autoBaseExec(String testName, Object obj, String fieldName)
      throws JCUnitCheckedException {
    return isAlreadyStored(obj, fieldName, testName);
  }

  private boolean isAlreadyStored(Object obj, String fieldName, String testName)
      throws JCUnitCheckedException {
    return fileForField(baseDir(), testName, field(obj.getClass(), fieldName))
        .exists();
  }
}

package com.github.dakusui.petronia.ut.forms;

import com.github.dakusui.jcunit.compat.core.JCUnitBase;
import com.github.dakusui.jcunit.exceptions.JCUnitCheckedException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.Form;
import com.github.dakusui.lisj.exceptions.LisjCheckedException;
import org.junit.Before;

public abstract class FormTestBase {
  private Form    form;
  private Context context;

  protected abstract Form createForm();

  protected Context createContext() {
    return new JCUnitBase();
  }

  @Before
  public void setUp() {
    this.form = createForm();
    this.context = createContext();
  }

  /**
   * This method should be used from inside each test method.
   *
   * @throws CUT
   */
  protected Object eval(Object... params) throws LisjCheckedException, CUT {
    return Basic.eval(context, this.form.bind(params));
  }

  /**
   * @param params
   * @return
   * @throws CUT
   */
  protected boolean evalp(Object... params) throws LisjCheckedException, CUT {
    return Basic.evalp(context, this.form.bind(params));
  }
}

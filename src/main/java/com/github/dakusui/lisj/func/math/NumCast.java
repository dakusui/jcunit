package com.github.dakusui.lisj.func.math;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;
import com.github.dakusui.lisj.LisjUtils;
import com.github.dakusui.lisj.func.BaseFunc;

import java.math.BigDecimal;
import java.math.BigInteger;

import static com.github.dakusui.lisj.Basic.get;
import static com.github.dakusui.lisj.Basic.length;

public abstract class NumCast extends BaseFunc {

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1443793883718558666L;

  @Override
  protected Object checkParams(Object params) {
    Utils.checknotnull(params);
    if (length(params) != 1) {
      throw new IllegalArgumentException(msgParameterLengthWrong(1, params));
    }
    if (get(params, 0) == null) {
      throw new IllegalArgumentException(msgFirstParameterIsNull(params));
    }
    return params;
  }

  @Override
  protected FormResult evaluateLast(Context context, Object[] evaluatedParams,
      FormResult lastResult) {
    Number value = Utils.bigDecimal(LisjUtils
        .cast(Number.class, evaluatedParams[0]));
    lastResult.value(cast(value));
    return lastResult;
  }

  abstract protected Number cast(Number value);

  public static NumCast intValue() {
    return (NumCast) new NumCast() {
      private static final long serialVersionUID = 7924501536036238003L;

      @Override
      protected Integer cast(Number value) {
        return value.intValue();
      }

      @Override
      public String name() {
        return "intValue";
      }
    };
  }

  public static NumCast shortValue() {
    return (NumCast) new NumCast() {
      private static final long serialVersionUID = 6348395673503341773L;

      @Override
      protected Short cast(Number value) {
        return value.shortValue();
      }

      @Override
      public String name() {
        return "shortValue";
      }
    };
  }

  public static NumCast longValue() {
    return (NumCast) new NumCast() {
      private static final long serialVersionUID = 2637219747541285577L;

      @Override
      protected Long cast(Number value) {
        return value.longValue();
      }

      @Override
      public String name() {
        return "longValue";
      }
    };
  }

  public static NumCast byteValue() {
    return (NumCast) new NumCast() {
      private static final long serialVersionUID = -5880928024002349262L;

      @Override
      protected Byte cast(Number value) {
        return value.byteValue();
      }

      @Override
      public String name() {
        return "byteValue";
      }
    };
  }

  public static NumCast doubleValue() {
    return (NumCast) new NumCast() {
      private static final long serialVersionUID = -6719786533694746176L;

      @Override
      protected Double cast(Number value) {
        return value.doubleValue();
      }

      @Override
      public String name() {
        return "doubleValue";
      }
    };
  }

  public static NumCast floatValue() {
    return (NumCast) new NumCast() {
      private static final long serialVersionUID = 6544097137166309639L;

      @Override
      protected Float cast(Number value) {
        return value.floatValue();
      }

      @Override
      public String name() {
        return "floatValue";
      }
    };
  }

  public static NumCast bigDecimal() {
    return (NumCast) new NumCast() {
      private static final long serialVersionUID = 8094063785372520905L;

      @Override
      protected BigDecimal cast(Number value) {
        return Utils.bigDecimal(value);
      }

      @Override
      public String name() {
        return "bigDecimal";
      }
    };
  }

  public static NumCast bigInteger() {
    return (NumCast) new NumCast() {
      private static final long serialVersionUID = 2641750630880999212L;

      @Override
      protected BigInteger cast(Number value) {
        return Utils.bigDecimal(value).toBigInteger();
      }

      @Override
      public String name() {
        return "bigInteger";
      }
    };
  }
}

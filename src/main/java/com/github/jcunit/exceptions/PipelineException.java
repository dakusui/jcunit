package com.github.jcunit.exceptions;

import static java.lang.String.format;

public class PipelineException extends FrameworkException {
  public PipelineException(String format) {
    super(format);
  }
}

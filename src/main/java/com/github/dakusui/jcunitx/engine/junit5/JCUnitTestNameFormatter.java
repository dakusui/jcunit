package com.github.dakusui.jcunitx.engine.junit5;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.StringUtils;

import java.text.Format;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.stream.IntStream;

import static com.github.dakusui.jcunitx.annotations.CombinatorialTest.*;
import static java.util.stream.Collectors.joining;

public class JCUnitTestNameFormatter {
  private final String pattern;
  private final String displayName;

  JCUnitTestNameFormatter(String pattern, String displayName) {
    this.pattern = pattern;
    this.displayName = displayName;
  }

  String format(int invocationIndex, Object... arguments) {
    try {
      return formatSafely(invocationIndex, arguments);
    } catch (Exception ex) {
      String message = "The display name pattern defined for the parameterized test is invalid. "
          + "See nested exception for further details.";
      throw new JUnitException(message, ex);
    }
  }

  private String formatSafely(int invocationIndex, Object[] arguments) {
    String pattern = prepareMessageFormatPattern(invocationIndex, arguments);
    MessageFormat format = new MessageFormat(pattern);
    Object[] humanReadableArguments = makeReadable(format, arguments);
    return format.format(humanReadableArguments);
  }

  private String prepareMessageFormatPattern(int invocationIndex, Object[] arguments) {
    String result = pattern//
        .replace(DISPLAY_NAME_PLACEHOLDER, this.displayName)//
        .replace(INDEX_PLACEHOLDER, String.valueOf(invocationIndex));

    if (result.contains(ARGUMENTS_PLACEHOLDER)) {
      // @formatter:off
      String replacement = IntStream.range(0, arguments.length)
          .mapToObj(index -> "{" + index + "}")
          .collect(joining(", "));
      // @formatter:on
      result = result.replace(ARGUMENTS_PLACEHOLDER, replacement);
    }

    return result;
  }

  private Object[] makeReadable(MessageFormat format, Object[] arguments) {
    Format[] formats = format.getFormatsByArgumentIndex();
    Object[] result = Arrays.copyOf(arguments, Math.min(arguments.length, formats.length), Object[].class);
    for (int i = 0; i < result.length; i++) {
      if (formats[i] == null) {
        result[i] = StringUtils.nullSafeToString(arguments[i]);
      }
    }
    return result;
  }
}

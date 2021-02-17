package com.github.dakusui.jcunitx.annotations;

import com.github.dakusui.jcunitx.engine.junit5.JCUnitExtension;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation marks a method that it can be run as a combinatorial test.
 *
 * @since 1.0.0
 */
@Retention(RUNTIME)
@TestTemplate
@Target(METHOD)
@ExtendWith(JCUnitExtension.class)
public @interface Combinatorial {
  String DISPLAY_NAME_PLACEHOLDER = "{displayName}";

  /**
   * Placeholder for the current invocation index of a {@code @ParameterizedTest}
   * method (1-based): <code>{index}</code>
   *
   * @see #name
   * @since 5.3
   */
  String INDEX_PLACEHOLDER = "{index}";

  /**
   * Placeholder for the complete, comma-separated arguments list of the
   * current invocation of a {@code @ParameterizedTest} method:
   * <code>{arguments}</code>
   *
   * @see #name
   * @since 5.3
   */
  String ARGUMENTS_PLACEHOLDER = "{arguments}";

  /**
   * Default display name pattern for the current invocation of a
   * {@code @ParameterizedTest} method: {@value}
   *
   * <p>Note that the default pattern does <em>not</em> include the
   * {@linkplain #DISPLAY_NAME_PLACEHOLDER display name} of the
   * {@code @ParameterizedTest} method.
   *
   * @see #name
   * @see #DISPLAY_NAME_PLACEHOLDER
   * @see #INDEX_PLACEHOLDER
   * @see #ARGUMENTS_PLACEHOLDER
   * @since 5.3
   */
  String DEFAULT_DISPLAY_NAME = "[" + INDEX_PLACEHOLDER + "] " + ARGUMENTS_PLACEHOLDER;

  /**
   * The display name to be used for individual invocations of the
   * parameterized test; never blank or consisting solely of whitespace.
   *
   * <p>Defaults to {@link #DEFAULT_DISPLAY_NAME}.
   *
   * <h4>Supported placeholders</h4>
   * <ul>
   * <li>{@link #DISPLAY_NAME_PLACEHOLDER}</li>
   * <li>{@link #INDEX_PLACEHOLDER}</li>
   * <li>{@link #ARGUMENTS_PLACEHOLDER}</li>
   * <li><code>{0}</code>, <code>{1}</code>, etc.: an individual argument (0-based)</li>
   * </ul>
   *
   * <p>For the latter, you may use {@link java.text.MessageFormat} patterns
   * to customize formatting.
   *
   * @see java.text.MessageFormat
   */
  String name() default DEFAULT_DISPLAY_NAME;


















}

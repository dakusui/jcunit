package com.github.jcunit.core.model;

import com.github.jcunit.annotations.DefineParameter;
import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.factorspace.Parameter;
import com.github.jcunit.testsuite.SchemafulTupleSet;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.jcunit.runners.helpers.ParameterUtils.simple;
import static com.github.jcunit.utils.Checks.wrap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * // @formatter:off
 *
 * // @formatter:on
 *
 * @param <G> Generation-time parameter type.
 * @param <E> Execution-time parameter type.
 */
public interface ParameterResolver<P extends Parameter<G>, G, E> {
}

package com.github.jcunit.annotations;

import com.github.jcunit.runners.junit5.JCUnitTestEngine;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@ExtendWith(JCUnitTestEngine.class)
@TestTemplate
@Retention(RUNTIME)
@Target(METHOD)
public @interface JCUnitTest {
}

package com.github.jcunit.annotations;

import com.github.jcunit.runners.junit5.JCUnitTestEngine;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@ExtendWith(JCUnitTestEngine.class)
@TestTemplate
@Retention(RUNTIME)
public @interface JCUnitTest {
}

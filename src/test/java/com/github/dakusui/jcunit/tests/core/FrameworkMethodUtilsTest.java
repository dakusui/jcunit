package com.github.dakusui.jcunit.tests.core;

import com.github.dakusui.jcunit.core.FrameworkMethodUtils;
import com.github.dakusui.jcunit.core.Given;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.util.List;

import static junit.framework.TestCase.assertTrue;

public class FrameworkMethodUtilsTest {
    public static class ValidatorTestClass {
        @SuppressWarnings("unused")
        public static boolean precondition1() {
            return true;
        }

        @SuppressWarnings("unused")
        public static boolean precondition2() {
            return true;
        }

        @SuppressWarnings("unused")
        public static boolean precondition3() {
            return true;
        }

        @Test
        @Given("precondition1")
        public void scenario1() {
        }

        @Test
        @Given("precondition2&&precondition3")
        public void scenario2() {
        }

        @Test
        @Given("!precondition2&&precondition3")
        public void scenario3() {
        }

        @Test
        @Given("precondition2&&!precondition3")
        public void scenario4() {
        }

        @Test
        @Given({"!precondition2&&!precondition3", "precondition1"})
        public void scenario5() {
        }
    }

    @Test
    public void test() {
        List<FrameworkMethod> methodList = FrameworkMethodUtils.FrameworkMethodValidator.REFERENCED_BY_GIVEN.getMethods(new TestClass(ValidatorTestClass.class));
        assertTrue(methodListContainsItemWhoseNameIsSpecified(methodList, "precondition1"));
    }

    private boolean methodListContainsItemWhoseNameIsSpecified(List<FrameworkMethod> methodList, String methodName) {
        for (FrameworkMethod each : methodList) {
            if (each.getName().equals(methodName)) {
                return true;
            }
        }
        return false;
    }
}

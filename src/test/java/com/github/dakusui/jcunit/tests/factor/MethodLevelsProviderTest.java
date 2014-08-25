package com.github.dakusui.jcunit.framework.factor;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.factor.LevelsProvider;
import com.github.dakusui.jcunit.core.factor.MethodLevelsProvider;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 */
public class MethodLevelsProviderTest {
    public static class TargetClass {
        @SuppressWarnings("unused")
        @FactorField
        public int targetField;

        @SuppressWarnings("unused")
        public static int[] targetField() {
            return new int[]{1, 10, 256};
        }
    }

    @Test
    public void test() throws NoSuchFieldException {
        TargetClass targetObject = new TargetClass();
        LevelsProvider factory = new MethodLevelsProvider();
        factory.setTargetField(targetObject.getClass().getField("targetField"));
        factory.setAnnotation(targetObject.getClass().getField("targetField").getAnnotation(FactorField.class));
        factory.init(new Object[]{});
        assertEquals(3, factory.size());
        assertEquals(1, factory.get(0));
        assertEquals(10, factory.get(1));
        assertEquals(256, factory.get(2));
    }
}

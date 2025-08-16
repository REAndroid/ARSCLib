package com.reandroid.arsc.value;

import org.junit.Assert;
import org.junit.Test;

public class AttributeTypeTest {
    private final static AttributeType[] PLURALS = new AttributeType[]{
            AttributeType.OTHER,
            AttributeType.ZERO,
            AttributeType.ONE,
            AttributeType.TWO,
            AttributeType.FEW,
            AttributeType.MANY
    };
    private final static AttributeType[] NON_PLURALS = new AttributeType[]{
            AttributeType.FORMATS,
            AttributeType.MIN,
            AttributeType.MAX,
            AttributeType.L10N
    };

    @Test
    public void testIsPlural() {
        for(AttributeType type : PLURALS) {
            Assert.assertTrue(type.isPlural());
        }

        for(AttributeType type : NON_PLURALS) {
            Assert.assertFalse(type.isPlural());
        }
    }
}

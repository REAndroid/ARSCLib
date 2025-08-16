package com.reandroid.arsc.value;

import org.junit.Assert;
import org.junit.Test;

public class ValueTypeTest {
    private static final ValueType[] ALL = ValueType.values();

    @Test
    public void testIsColor() {
        for(ValueType type : ALL) {
            Assert.assertEquals(isColor(type), type.isColor());
        }
    }

    @Test
    public void testIsInteger() {
        for(ValueType type : ALL) {
            Assert.assertEquals(isInteger(type), type.isInteger());
        }
    }

    @Test
    public void testIsReference() {
        for(ValueType type : ALL) {
            Assert.assertEquals(isReference(type), type.isReference());
        }
    }

    private static boolean isColor(ValueType valueType) {
        return valueType == ValueType.COLOR_ARGB8
                || valueType == ValueType.COLOR_RGB8
                || valueType == ValueType.COLOR_ARGB4
                || valueType == ValueType.COLOR_RGB4;
    }

    private static boolean isInteger(ValueType valueType) {
        return valueType == ValueType.DEC
                || valueType == ValueType.HEX;
    }

    private static boolean isReference(ValueType valueType) {
        return valueType == ValueType.REFERENCE
                || valueType == ValueType.ATTRIBUTE
                || valueType == ValueType.DYNAMIC_REFERENCE
                || valueType == ValueType.DYNAMIC_ATTRIBUTE;
    }
}

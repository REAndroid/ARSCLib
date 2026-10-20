package com.reandroid.arsc.value;

import org.junit.Assert;
import org.junit.Test;

public class DataTypeTest {
    private static final DataType[] ALL = DataType.values();

    @Test
    public void testIsColor() {
        for(DataType type : ALL) {
            Assert.assertEquals(isColor(type), type.isColor());
        }
    }

    @Test
    public void testIsInteger() {
        for(DataType type : ALL) {
            Assert.assertEquals(isInteger(type), type.isInteger());
        }
    }

    @Test
    public void testIsReference() {
        for(DataType type : ALL) {
            Assert.assertEquals(isReference(type), type.isReference());
        }
    }

    private static boolean isColor(DataType dataType) {
        return dataType == DataType.COLOR_ARGB8
                || dataType == DataType.COLOR_RGB8
                || dataType == DataType.COLOR_ARGB4
                || dataType == DataType.COLOR_RGB4;
    }

    private static boolean isInteger(DataType dataType) {
        return dataType == DataType.DEC
                || dataType == DataType.HEX;
    }

    private static boolean isReference(DataType dataType) {
        return dataType == DataType.REFERENCE
                || dataType == DataType.ATTRIBUTE
                || dataType == DataType.DYNAMIC_REFERENCE
                || dataType == DataType.DYNAMIC_ATTRIBUTE;
    }
}

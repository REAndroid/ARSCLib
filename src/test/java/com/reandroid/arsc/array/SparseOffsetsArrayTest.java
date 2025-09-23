package com.reandroid.arsc.array;

import org.junit.Assert;
import org.junit.Test;

public class SparseOffsetsArrayTest {
    @Test
    public void testSetOffset() {
        //offsets are encoded is 16-bit, real_offset = offset * 4
        final SparseOffsetsArray sparseArr = new SparseOffsetsArray();

        final int offset = 0x10;
        final int index = 0x0000;

        //put data
        final int data = ((offset / 4) << 16) | index;
        sparseArr.set(new int[]{ data });
        Assert.assertEquals(offset, sparseArr.getOffset(0));

        //test setOffset
        sparseArr.clear();
        sparseArr.setSize(1);
        sparseArr.setOffset(0, offset);
        Assert.assertEquals(offset, sparseArr.getOffset(0));
    }
}

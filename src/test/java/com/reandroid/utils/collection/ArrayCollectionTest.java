package com.reandroid.utils.collection;

import org.junit.Assert;
import org.junit.Test;

public class ArrayCollectionTest {

    @Test
    public void testInsertAtIndexThenSetSize() {
        ArrayCollection<Integer> collection = new ArrayCollection<>();
        for (int i = 0; i < 1000; i++) {
            collection.add(i / 2, i);
        }
        Assert.assertEquals(1000, collection.size());
        // insertions leave spare capacity behind, growing must still fit
        collection.setSize(1500);
        Assert.assertEquals(1500, collection.size());
        collection.ensureCapacity(10);
        Assert.assertTrue(collection.availableCapacity() >= 10);
        int free = collection.availableCapacity();
        collection.ensureCapacity(free + 5);
        Assert.assertTrue(collection.availableCapacity() >= free + 5);
        collection.ensureCapacity(free + 2);
        Assert.assertTrue(collection.availableCapacity() >= free + 5);
    }

    @Test
    public void testInsertOrder() {
        ArrayCollection<Integer> collection = new ArrayCollection<>();
        for (int i = 0; i < 100; i++) {
            collection.add(0, i);
        }
        for (int i = 0; i < 100; i++) {
            Assert.assertEquals(Integer.valueOf(99 - i), collection.get(i));
        }
    }
}

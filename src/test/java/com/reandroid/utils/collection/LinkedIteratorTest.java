package com.reandroid.utils.collection;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

public class LinkedIteratorTest {

    @Test
    public void testLinkedItem() {
        LinkedItem item =
                new LinkedItem(null, "H")
                .newChild("G")
                .newChild("F")
                .newChild("E")
                .newChild("D")
                .newChild("C")
                .newChild("B")
                .newChild("A");

        Iterator<LinkedItem> iterator = LinkedIterator.of(item, LinkedItem::getParent);
        List<LinkedItem> itemList = CollectionUtil.collect(iterator);
        Assert.assertEquals("itemList size", 7, itemList.size());
        iterator = LinkedIterator.of(true, item, LinkedItem::getParent);

        List<LinkedItem> itemListIncludingSelf = CollectionUtil.collect(iterator);
        Assert.assertEquals("itemListIncludingSelf size", 8, itemListIncludingSelf.size());
    }

    static class LinkedItem {

        public final String name;
        private final LinkedItem parent;

        public LinkedItem(LinkedItem parent, String name) {
            this.parent = parent;
            this.name = name;
        }

        public LinkedItem newChild(String name) {
            return new LinkedItem(this, name);
        }

        public String getName() {
            return name;
        }
        public LinkedItem getParent() {
            return parent;
        }

        @Override
        public String toString() {
            LinkedItem parent = getParent();
            if (parent == null) {
                return getName();
            }
            return parent + "/" + getName();
        }
    }
}

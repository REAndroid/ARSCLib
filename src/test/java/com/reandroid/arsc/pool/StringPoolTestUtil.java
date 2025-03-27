package com.reandroid.arsc.pool;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.item.ReferenceItem;
import com.reandroid.arsc.item.StringItem;

import java.util.Iterator;

public class StringPoolTestUtil {


    public static ReferenceItem newFakeReference() {
        return new ReferenceItem() {
            private int mValue;
            @Override
            public <T1 extends Block> T1 getReferredParent(Class<T1> parentClass) {
                return null;
            }
            @Override
            public int get() {
                return mValue;
            }
            @Override
            public void set(int value) {
                this.mValue = value;
            }
            @Override
            public String toString() {
                return "Fake reference: " + get();
            }
        };
    }

    // param label: No real purpose, just to show on toString while debugging
    public static ReferenceItem newFakeReference(Object label) {
        return new ReferenceItem() {
            private int mValue;
            @Override
            public <T1 extends Block> T1 getReferredParent(Class<T1> parentClass) {
                return null;
            }
            @Override
            public int get() {
                return mValue;
            }
            @Override
            public void set(int value) {
                this.mValue = value;
            }
            @Override
            public String toString() {
                return "[Fake reference-" + label + "] " + get();
            }
        };
    }

    public static ReferenceItem getFirstFakeReference(StringItem stringItem) {
        Iterator<ReferenceItem> iterator = stringItem.getReferences();
        while (iterator.hasNext()) {
            ReferenceItem referenceItem = iterator.next();
            if (referenceItem.toString().contains("Fake reference")) {
                return referenceItem;
            }
        }
        return null;
    }
    public static String to4WidthString(int i) {
        if (i < 10) {
            return "000" + i;
        }
        if (i < 100) {
            return "00" + i;
        }
        if (i < 1000) {
            return "0" + i;
        }
        return Integer.toString(i);
    }
}

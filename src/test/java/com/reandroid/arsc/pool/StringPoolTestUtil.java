package com.reandroid.arsc.pool;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.item.ReferenceItem;

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
}

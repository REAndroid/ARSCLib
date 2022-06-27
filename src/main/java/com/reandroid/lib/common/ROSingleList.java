package com.reandroid.lib.common;

import java.util.AbstractList;

public class ROSingleList<T> extends AbstractList<T> {
    private final T item;
    public ROSingleList(T item){
        this.item=item;
    }
    @Override
    public T get(int i) {
        if(i==0 && this.item!=null){
            return this.item;
        }
        throw new ArrayIndexOutOfBoundsException(getClass().getSimpleName()+": "+i);
    }

    @Override
    public int size() {
        if(this.item==null){
            return 0;
        }
        return 1;
    }
}

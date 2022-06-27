package com.reandroid.lib.common;

import java.util.AbstractList;

public class ROArrayList<T> extends AbstractList<T> {
    private final T[] elementData;
    public ROArrayList(T[] elementData){
        this.elementData=elementData;
    }
    @Override
    public T get(int i) {
        return this.elementData[i];
    }
    @Override
    public int size() {
        if(this.elementData==null){
            return 0;
        }
        return this.elementData.length;
    }
}

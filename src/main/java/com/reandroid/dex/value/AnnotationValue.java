package com.reandroid.dex.value;

import com.reandroid.dex.item.AnnotationItem;
import com.reandroid.dex.key.AnnotationKey;

public class AnnotationValue extends DexValueBlock<AnnotationItem> {

    public AnnotationValue() {
        super(new AnnotationItem(true), DexValueType.ANNOTATION);
    }
    public AnnotationItem get(){
        return getValueContainer();
    }
    public AnnotationKey getKey(){
        AnnotationItem item = get();
        if(item != null){
            return item.getKey();
        }
        return null;
    }

    @Override
    public DexValueType<?> getValueType() {
        return DexValueType.ANNOTATION;
    }
    @Override
    public String getAsString() {
        AnnotationKey key = getKey();
        if(key != null){
            return key.toString();
        }
        return null;
    }
}

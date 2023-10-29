package com.reandroid.dex.value;

import com.reandroid.dex.data.AnnotationItem;
import com.reandroid.dex.key.DataKey;

public class AnnotationValue extends DexValueBlock<AnnotationItem> {

    public AnnotationValue() {
        super(new AnnotationItem(true), DexValueType.ANNOTATION);
    }
    public AnnotationItem get(){
        return getValueContainer();
    }
    public DataKey<AnnotationItem> getKey(){
        return get().getKey();
    }

    @Override
    public DexValueType<?> getValueType() {
        return DexValueType.ANNOTATION;
    }
    @Override
    public String getAsString() {
        return get().toString();
    }
    @Override
    public void merge(DexValueBlock<?> valueBlock){
        super.merge(valueBlock);
        AnnotationValue value = (AnnotationValue) valueBlock;
        AnnotationItem coming = value.get();
        AnnotationItem item = get();
        item.setType(coming.getTypeKey());
        item.merge(coming);
    }
}
